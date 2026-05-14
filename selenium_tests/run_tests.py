#!/usr/bin/env python
"""
Test runner script for EduQuiz Selenium tests.
Run with: python run_tests.py [options]
"""
import subprocess
import argparse
import sys


def run_command(cmd: str) -> int:
    """Run a shell command and return exit code."""
    print(f"Running: {cmd}")
    result = subprocess.run(cmd, shell=True)
    return result.returncode


def main():
    parser = argparse.ArgumentParser(description="EduQuiz Selenium Test Runner")

    parser.add_argument(
        "--smoke",
        action="store_true",
        help="Run only smoke tests"
    )
    parser.add_argument(
        "--deck",
        action="store_true",
        help="Run only deck tests"
    )
    parser.add_argument(
        "--quiz",
        action="store_true",
        help="Run only quiz tests"
    )
    parser.add_argument(
        "--flashcard",
        action="store_true",
        help="Run only flashcard tests"
    )
    parser.add_argument(
        "--settings",
        action="store_true",
        help="Run only settings tests"
    )
    parser.add_argument(
        "--file",
        type=str,
        help="Run specific test file (e.g., test_smoke.py)"
    )
    parser.add_argument(
        "--headless",
        action="store_true",
        default=True,
        help="Run in headless mode (default: True)"
    )
    parser.add_argument(
        "--headed",
        action="store_true",
        help="Run with browser window visible"
    )
    parser.add_argument(
        "--verbose",
        "-v",
        action="store_true",
        help="Verbose output"
    )
    parser.add_argument(
        "--failed",
        "-lf",
        action="store_true",
        help="Run only failed tests from last run"
    )
    parser.add_argument(
        "--pdb",
        action="store_true",
        help="Drop into debugger on failures"
    )

    args = parser.parse_args()

    # Build pytest command
    cmd = "pytest"

    # Add test file if specified
    if args.file:
        cmd += f" {args.file}"
    else:
        # Add markers
        markers = []
        if args.smoke:
            markers.append("smoke")
        if args.deck:
            markers.append("deck")
        if args.quiz:
            markers.append("quiz")
        if args.flashcard:
            markers.append("flashcard")
        if args.settings:
            markers.append("settings")

        if markers:
            marker_expr = " or ".join(markers)
            cmd += f" -m '{marker_expr}'"

    # Add options
    if args.verbose:
        cmd += " -v"

    if args.failed:
        cmd += " --lf"

    if args.pdb:
        cmd += " --pdb"

    # Set environment variable for headless mode
    if args.headed:
        cmd = "HEADLESS=False " + cmd

    # Run the command
    exit_code = run_command(cmd)
    return exit_code


if __name__ == "__main__":
    sys.exit(main())

