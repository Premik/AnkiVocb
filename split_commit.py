#!/usr/bin/env python3

import sys
from pathlib import Path
from typing import List, Optional

import git  # type: ignore[import-untyped] # GitPython can be tricky with type checkers


class SplitCommitError(Exception):
    """Custom exception for errors during commit splitting."""

    pass


def _get_commit_to_split(repo: git.Repo) -> git.Commit:
    """Retrieves the previous commit (HEAD~1) to be split."""
    try:
        # Always use HEAD~1 (previous commit)
        if len(repo.head.commit.parents) == 0:
            raise SplitCommitError("Error: HEAD has no parent commit to split.")
        return repo.head.commit
    except git.exc.BadName as e:
        raise SplitCommitError(f"Error: Could not find the previous commit. {e}") from e
    except Exception as e:
        raise SplitCommitError(
            f"An unexpected error occurred while finding the commit: {e}"
        ) from e


def _apply_diff_item(
    repo: git.Repo, diff_item: git.diff.Diff, original_commit_hexsha: str
) -> None:
    """Applies a single diff item to the index."""
    if diff_item.renamed_file:  # 'R' type (Rename or Copy)
        if diff_item.a_path:
            repo.index.remove([str(Path(diff_item.a_path))], working_tree=True)
        if diff_item.b_path:
            repo.git.checkout(original_commit_hexsha, "--", str(Path(diff_item.b_path)))
            repo.git.add(str(Path(diff_item.b_path)))
    elif diff_item.deleted_file:  # 'D' type
        if diff_item.a_path:
            repo.index.remove([str(Path(diff_item.a_path))], working_tree=True)
    elif diff_item.new_file:  # 'A' type
        if diff_item.b_path:
            repo.git.checkout(original_commit_hexsha, "--", str(Path(diff_item.b_path)))
            repo.git.add(str(Path(diff_item.b_path)))
    elif diff_item.change_type in ("M", "T"):  # Modified or Typechange
        # For 'M' and 'T', a_path and b_path are typically the same.
        # Checkout path should be the one present in the commit_to_split.
        path_to_checkout = (
            diff_item.b_path or diff_item.a_path
        )  # b_path is preferred if available
        if path_to_checkout:
            repo.git.checkout(original_commit_hexsha, "--", str(Path(path_to_checkout)))
            repo.git.add(str(Path(path_to_checkout)))
    else:
        # This case should ideally not be reached with standard diff types.
        # Includes 'C' (Copied) if not handled as renamed_file, 'U' (Unmerged), etc.
        # For simplicity, we raise an error for unhandled types.
        raise SplitCommitError(
            f"Unhandled diff type '{diff_item.change_type}' for "
            f"{diff_item.a_path or ''} -> {diff_item.b_path or ''}"
        )


def split_single_commit(repo: git.Repo) -> None:
    """
    Splits the HEAD commit into multiple commits, one for each changed file/operation.
    """
    try:
        commit_to_split = _get_commit_to_split(repo)
    except SplitCommitError as e:
        print(str(e), file=sys.stderr)
        sys.exit(1)

    if not commit_to_split.parents:
        print(
            "Error: Splitting an initial commit (a commit with no parents) is not supported.",
            file=sys.stderr,
        )
        sys.exit(1)

    parent_commit_sha = commit_to_split.parents[0].hexsha

    # R=True to detect renames as a single diff item.
    # create_patch=False as we don't need the patch text, just file info.
    diffs_to_apply: List[git.diff.Diff] = list(
        commit_to_split.diff(parent_commit_sha, create_patch=False, R=True)
    )

    if not diffs_to_apply:
        print(
            "The commit has no changes to split (compared to its parent).",
            file=sys.stderr,
        )
        return

    original_message = commit_to_split.message.strip()
    author = commit_to_split.author
    committer = commit_to_split.committer

    print(f'Target commit: {commit_to_split.hexsha[:7]} "{commit_to_split.summary}"')
    print(f"Parent commit: {parent_commit_sha[:7]}")
    print(f"Found {len(diffs_to_apply)} changes to split into parts.")

    # Reset HEAD to the parent of the commit we are splitting.
    # This makes the working directory and index match the parent.
    print(f"Resetting current branch to parent commit {parent_commit_sha[:7]}...")
    try:
        repo.git.reset("--hard", parent_commit_sha)
    except git.exc.GitCommandError as e:
        print(f"Error resetting to parent commit: {e}", file=sys.stderr)
        sys.exit(1)

    for i, diff_item in enumerate(diffs_to_apply):
        part_num = i + 1
        current_commit_message = f"{original_message} - part-{part_num}"

        change_description = f"{diff_item.change_type} "
        if diff_item.a_path:
            change_description += str(Path(diff_item.a_path))
        if (
            diff_item.renamed_file and diff_item.b_path
        ):  # For renames, show "old -> new"
            change_description += f" -> {str(Path(diff_item.b_path))}"
        elif (
            diff_item.a_path
            and diff_item.b_path
            and diff_item.a_path != diff_item.b_path
        ):
            # Should not happen if rename is caught by R=True, but as fallback
            change_description += f" (to {str(Path(diff_item.b_path))})"
        elif diff_item.b_path and not diff_item.a_path:  # New file
            change_description += str(Path(diff_item.b_path))

        print(
            f"\nProcessing part {part_num}/{len(diffs_to_apply)}: {change_description.strip()}"
        )

        try:
            _apply_diff_item(repo, diff_item, commit_to_split.hexsha)

            # Commit this one change
            # Using skip_hooks=True to prevent pre-commit hooks from interfering
            repo.index.commit(
                current_commit_message,
                author=author,
                committer=committer,
                skip_hooks=True,
            )
            print(
                f'  Committed as: {repo.head.commit.hexsha[:7]} "{repo.head.commit.summary}"'
            )

            print("  Pushing to GitHub masternew branch...")
            repo.git.push("github", "HEAD:masternew", force=True)
            print("  Successfully pushed to GitHub masternew")

        except (git.exc.GitCommandError, SplitCommitError) as e:
            print(f"  Error processing part {part_num}: {e}", file=sys.stderr)
            print(
                "  The repository may be in an inconsistent state. Manual intervention might be required.",
                file=sys.stderr,
            )
            print(
                f"  Attempted to process: {change_description.strip()}", file=sys.stderr
            )
            sys.exit(1)
        except Exception as e:  # Catch any other unexpected error during apply/commit
            print(
                f"  An unexpected error occurred processing part {part_num} ({change_description.strip()}): {e}",
                file=sys.stderr,
            )
            print(
                "  The repository may be in an inconsistent state. Manual intervention might be required.",
                file=sys.stderr,
            )
            sys.exit(1)

    print(
        f'\nSplit complete. New HEAD is: {repo.head.commit.hexsha[:7]} "{repo.head.commit.summary}"'
    )
    print("Please review the new commit history carefully.")


def main() -> None:

    try:
        # search_parent_directories=True allows running from within a subdirectory of the repo
        repo = git.Repo(".", search_parent_directories=True)
    except git.exc.InvalidGitRepositoryError:
        print(
            "Error: The current directory is not part of a Git repository.",
            file=sys.stderr,
        )
        sys.exit(1)
    except Exception as e:  # Catch other potential errors from Repo() instantiation
        print(f"Error initializing Git repository object: {e}", file=sys.stderr)
        sys.exit(1)

    split_single_commit(repo)


if __name__ == "__main__":
    main()
