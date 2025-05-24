import os
import subprocess
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import List, Set

# --- Configuration ---
SOURCE_BRANCH: str = "master"
TARGET_BRANCH: str = "masternew"
PICKED_COMMITS_FILE: Path = Path("picked_commits.txt")
REMOTE_NAME: str = "github"  # Or your specific remote name
GIT_AUTHOR_NAME: str = ""  # Set your desired author name here
GIT_AUTHOR_EMAIL: str = ""  # Set your desired email here
# --- End Configuration ---


@dataclass
class GitCommandError(Exception):
    """Custom exception for Git command failures."""

    command: List[str]
    stdout: str
    stderr: str
    returncode: int

    def __str__(self) -> str:
        return (
            f"Command '{' '.join(self.command)}' failed with exit code {self.returncode}\n"
            f"Stdout:\n{self.stdout}\n"
            f"Stderr:\n{self.stderr}"
        )


def run_git_command(
    args: List[str], check: bool = True, capture_output: bool = True, env: dict = None
) -> subprocess.CompletedProcess:
    """Runs a Git command and handles potential errors."""
    try:
        process = subprocess.run(
            ["git"] + args,
            check=check,
            capture_output=capture_output,
            text=True,
            encoding="utf-8",
            env=env,
        )
        return process
    except subprocess.CalledProcessError as e:
        raise GitCommandError(
            command=["git"] + args,
            stdout=e.stdout or "",
            stderr=e.stderr or "",
            returncode=e.returncode,
        ) from e


def get_commits_from_branch(branch_name: str) -> List[str]:
    """Fetches all commit SHAs from a given branch, oldest first."""
    try:
        # --reverse to get oldest first, --pretty=format:%H for just the hash
        result = run_git_command(
            ["log", branch_name, "--reverse", "--pretty=format:%H"]
        )
        commits = result.stdout.strip().split("\n")
        return [commit for commit in commits if commit]  # Filter out empty lines if any
    except GitCommandError as e:
        print(f"Error fetching commits from branch {branch_name}: {e}", file=sys.stderr)
        sys.exit(1)


def load_picked_commits(file_path: Path) -> Set[str]:
    """Loads already picked commit SHAs from the specified file."""
    if not file_path.exists():
        return set()
    with file_path.open("r", encoding="utf-8") as f:
        return {line.strip() for line in f if line.strip()}


def save_picked_commit(commit_sha: str, file_path: Path) -> None:
    """Appends a successfully picked commit SHA to the file."""
    with file_path.open("a", encoding="utf-8") as f:
        f.write(f"{commit_sha}\n")


def main() -> None:
    """Main script execution."""
    picked_commits: Set[str] = load_picked_commits(PICKED_COMMITS_FILE)
    print(f"Loaded {len(picked_commits)} already picked commits.")

    source_commits: List[str] = get_commits_from_branch(SOURCE_BRANCH)
    if not source_commits:
        print(f"No commits found on branch '{SOURCE_BRANCH}'. Exiting.")
        sys.exit(0)

    print(f"Found {len(source_commits)} commits on '{SOURCE_BRANCH}'.")

    commits_to_pick: List[str] = [
        commit for commit in source_commits if commit not in picked_commits
    ]

    if not commits_to_pick:
        print(
            "No new commits to pick. All commits from master seem to be picked already."
        )
        sys.exit(0)

    print(f"Attempting to pick {len(commits_to_pick)} new commits.")

    if not GIT_AUTHOR_NAME or not GIT_AUTHOR_EMAIL:
        try:
            user_name = run_git_command(["config", "user.name"]).stdout.strip()
            user_email = run_git_command(["config", "user.email"]).stdout.strip()
        except GitCommandError as e:
            print(f"Error getting local Git user info: {e}", file=sys.stderr)
            sys.exit(1)
    else:
        user_name = GIT_AUTHOR_NAME
        user_email = GIT_AUTHOR_EMAIL
    current_branch_process = run_git_command(["rev-parse", "--abbrev-ref", "HEAD"])
    initial_branch = current_branch_process.stdout.strip()

    for i, commit_sha in enumerate(commits_to_pick):
        print(f"\n[{i+1}/{len(commits_to_pick)}] Processing commit: {commit_sha}")

        # Ensure we are on the target branch
        print(f"Checking out '{TARGET_BRANCH}'...")
        try:
            run_git_command(["checkout", TARGET_BRANCH])
        except GitCommandError as e:
            print(f"Error checking out {TARGET_BRANCH}: {e}", file=sys.stderr)
            print("Please resolve the issue and rerun the script.")
            sys.exit(1)

            # Get the original commit message
        commit_msg = run_git_command(
            ["log", "-1", "--pretty=%B", commit_sha]
        ).stdout.strip()

        # Prepare environment for author override
        git_env = os.environ.copy()
        git_env.update(
            {
                "GIT_AUTHOR_NAME": user_name,
                "GIT_AUTHOR_EMAIL": user_email,
                "GIT_COMMITTER_NAME": user_name,
                "GIT_COMMITTER_EMAIL": user_email,
            }
        )

        try:
            # First, cherry-pick with --no-commit to stage the changes
            print(f"Staging changes from {commit_sha} using cherry-pick --no-commit...")
            # Use our run_git_command function with the env parameter
            run_git_command(
                [
                    "cherry-pick",
                    "--no-commit",  # Stage changes but don't commit yet
                    "--strategy=recursive",
                    "--strategy-option=theirs",
                    commit_sha,
                ]
            )
            print(f"Changes from {commit_sha} staged.")

            # Now commit the staged changes with the desired author and original message
            print(
                f"Committing staged changes with author {user_name} <{user_email}>..."
            )
            run_git_command(["commit", "-m", commit_msg], env=git_env)

            print(
                f"Successfully created new commit from {commit_sha} with author {user_name} <{user_email}>."
            )
        except GitCommandError as e:
            if "conflict" in e.stderr.lower() or "conflict" in e.stdout.lower():
                print(
                    f"\n!!! CONFLICT DETECTED while cherry-picking {commit_sha} despite using 'theirs' strategy !!!",
                    file=sys.stderr,
                )
                print(
                    "Attempting to abort the cherry-pick and continue with a different approach...",
                    file=sys.stderr,
                )
                try:
                    # Abort the failed cherry-pick
                    run_git_command(["cherry-pick", "--abort"])

                    # Alternative approach: checkout source files from the commit and commit them
                    print("Manually applying changes from the source commit...")

                    # Get the commit message for later use (ensure it's stripped)
                    original_commit_message_for_conflict = run_git_command(
                        ["log", "-1", "--pretty=%B", commit_sha]
                    ).stdout.strip()

                    # Create a temporary branch to get source files
                    temp_branch = f"temp-cherry-{commit_sha[:8]}"
                    run_git_command(["branch", temp_branch, commit_sha])

                    # Extract changes from the commit
                    run_git_command(["checkout", temp_branch, "--", "."])

                    # Commit the changes with the original commit message and correct author
                    print(
                        f"Committing manually applied changes (conflict resolution) with author {user_name} <{user_email}>..."
                    )
                    run_git_command(
                        ["commit", "-a", "-m", original_commit_message_for_conflict],
                        env=git_env,
                    )

                    # Clean up temp branch
                    run_git_command(["branch", "-D", temp_branch], check=False)

                    print(
                        f"Successfully applied changes from {commit_sha} manually with author {user_name} <{user_email}>."
                    )

                except GitCommandError as e2:
                    print(f"Failed to manually apply changes: {e2}", file=sys.stderr)
                    # Attempt to switch back to the initial branch before exiting
                    if initial_branch and initial_branch != TARGET_BRANCH:
                        try:
                            run_git_command(["checkout", initial_branch], check=False)
                        except GitCommandError:
                            pass  # Best effort
                    sys.exit(1)
            else:
                print(f"Error during cherry-pick of {commit_sha}: {e}", file=sys.stderr)
                # Attempt to switch back to the initial branch before exiting
                if initial_branch and initial_branch != TARGET_BRANCH:
                    try:
                        run_git_command(["checkout", initial_branch], check=False)
                    except GitCommandError:
                        pass  # Best effort
                sys.exit(1)

        print(f"Pushing '{TARGET_BRANCH}' to '{REMOTE_NAME}'...")
        try:
            run_git_command(["push", REMOTE_NAME, TARGET_BRANCH])
            print(f"Successfully pushed '{TARGET_BRANCH}'.")
        except GitCommandError as e:
            print(f"Error pushing {TARGET_BRANCH}: {e}", file=sys.stderr)
            print(f"Commit {commit_sha} was picked locally but not pushed.")
            print(
                "Please resolve the push issue and then manually add the commit to picked_commits.txt or re-run after fixing."
            )
            # Attempt to switch back to the initial branch before exiting
            if initial_branch and initial_branch != TARGET_BRANCH:
                try:
                    run_git_command(["checkout", initial_branch], check=False)
                except GitCommandError:
                    pass  # Best effort
            sys.exit(1)

        save_picked_commit(commit_sha, PICKED_COMMITS_FILE)
        print(f"Recorded {commit_sha} as picked.")
        if i > 100:
            break

    # Switch back to the initial branch if it was different
    current_branch_after_ops = run_git_command(
        ["rev-parse", "--abbrev-ref", "HEAD"]
    ).stdout.strip()
    if initial_branch and initial_branch != current_branch_after_ops:
        print(
            f"\nOperations complete. Switching back to initial branch '{initial_branch}'..."
        )
        try:
            run_git_command(["checkout", initial_branch])
        except GitCommandError as e:
            print(
                f"Could not switch back to initial branch {initial_branch}: {e}",
                file=sys.stderr,
            )

    print("\nAll specified commits have been processed.")


if __name__ == "__main__":
    main()
