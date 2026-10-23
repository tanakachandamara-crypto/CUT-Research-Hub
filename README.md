# CUT Research Hub

A Java Swing desktop project for sharing academic research and reviewing student contributions.

## Start on Windows

1. Extract the ZIP completely. Open `CUT Research Hub/CUTResearchHub`.
2. Install a desktop Java 17 or newer runtime/JDK if Java is not already available.
3. Double-click **run.bat**. It launches the included `CUTResearchHub.jar` from the correct data folder.
4. Select **New here? Create an account**. On a fresh installation, choose **Admin (first-account setup)** for your first account if you want access to user listing and backups. Subsequent registrations offer Student and Researcher.

No database server, Maven, or external libraries are required. Accounts are created by you; no preset passwords or test users are included.

## Edit and rebuild

Open this folder in IntelliJ IDEA or VS Code with Java support. Use **JDK 17 or newer** and run `src/App.java`, with this project folder as the working directory.

- Windows: run `build.bat`, then `run.bat`.
- macOS/Linux: run `sh build.sh`, then `sh run.sh`.
- Core regression tests: `test.bat` or `sh test.sh`.
- GUI smoke test on a desktop: after running the tests, use `java -cp "build/classes;build/test-classes" GuiSmokeTest` on Windows, or replace `;` with `:` on macOS/Linux.

`build.bat` and `build.sh` compile the editable sources and recreate the executable JAR. They do not delete saved data. Rebuild after source changes; `run.bat` otherwise runs the included JAR.

## A short demonstration

Use separate accounts with passwords of at least eight characters.

1. **Researcher:** register, create a repository called *Smart Irrigation*, then open it and add a paper titled *Soil Moisture Study*.
2. **Student:** sign out, register a Student account, find that repository using Search, select it and choose **Fork selected**.
3. Open **My repositories**, open the fork, and add a new paper with your findings. This creates a commit in the activity history.
4. Select the new paper and choose **Submit paper**. Give the pull request a meaningful title.
5. **Researcher:** sign back in, open **Pull requests**, read the contribution, then choose **Review selected → Merge**.
6. Open the original repository. The student's paper is now included. The request stays visible as **MERGED** rather than appearing pending again.
7. Show the profile totals, activity history, and light/dark modes. An Admin can also show the user list and create a separate backup.

Reject keeps the paper out of the target repository. Cancel leaves the request unchanged. The table can be searched and sorted; selection still targets the correct repository.

## Roles and rules

| Role | Main actions | Repository limit |
| --- | --- | --- |
| Student | Browse, read, fork, add papers to own forks, submit new papers | 5 forks |
| Researcher | Create repositories, add own papers, review contributions to own repositories | 20 |
| Admin | Create and manage papers, review all requests, list users, create backups | Unlimited |

Students submit a new paper from a fork of a researcher/admin repository. This version merges additions; it does not edit or merge revisions to existing papers. Researcher registration is self-selected for the classroom workflow. The initial setup administrator is created only while no accounts exist; existing administrator accounts continue to work.

## Structure 

- `model`: `User`, its Student/Researcher/Admin subclasses, `HubRepository`, `Paper`, `PullRequest`, and history actions.
- `core/Hub.java`: local storage, account registration, login, save/load, backups.
- `service/ResearchService.java`: repository ownership, quotas, forks, contributions and review rules.
- `gui/CUTResearchHubGUI.java`: screens, dialogs, navigation and UI events.
- `gui/Theme.java`: shared fonts, colours and component styling.
- `test`: dependency-free core regression checks and a desktop GUI smoke test.

The design demonstrates encapsulation, inheritance, abstract classes, polymorphism, composition, collections, exceptions and file persistence. It uses a model/service/view separation; it is not a full MVP implementation.

## Project scope

This is a **single-computer academic prototype**. Papers are typed text or summaries, not uploaded PDF/DOCX files. It is not a Git implementation and does not include branches, revision diffs, cloud synchronization, email verification, password reset or concurrent multi-user storage. Do not run multiple instances against the same data folder. Only load serialized data produced by your own trusted copy of this app. Desktop roles are application rules, not protection against someone who can directly edit the program or data files.
