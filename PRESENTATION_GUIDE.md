# Presenting CUT Research Hub

## Problem and aim (30–45 seconds)

“Students and researchers need a way to organise research and contribute to one another's work. CUT Research Hub is a desktop application where researchers create repositories, students work in their own copies, and repository owners review proposed contributions.”

## Show the working flow (3–4 minutes)

Prepare a researcher account and a student account before presenting. Show one repository, fork it as the student, add a new paper, and submit it. Sign in as the researcher, read and merge the contribution, then open the original repository to prove the paper was added. Finish by restarting the application to show persistence.

Use a small topic you understand, such as soil-moisture sensing, campus energy monitoring or a student accommodation study. Keep the paper text brief enough to read on screen.

## Explain the code (1–2 minutes)

| Concept | Example in the code |
| --- | --- |
| Encapsulation | Private model fields with methods such as `getPapers()` returning a copy of the list |
| Inheritance | `Student`, `Researcher` and `Admin` extend `User` |
| Abstraction | `User` declares role-specific methods; `Action` declares `describe()` |
| Polymorphism | `canCreateRepo()`, `canMerge()` and `getMaxRepos()` vary by role |
| Composition | A repository holds papers; a pull request refers to an author, target and proposed paper |
| Separation of responsibilities | Swing handles input; ResearchService checks rules; Hub handles storage |
| Exception handling | Invalid input and failed saves are reported without silently discarding data |
| Persistence | Four collections are serialized and restored when the application starts |

## Likely questions

**Why Swing?** It provides desktop controls within Java and makes the project runnable without a web server.

**How does a fork differ from a pull request?** A fork is the student's separate repository copy. A pull request proposes adding one new paper from that fork into the original repository.

**What does merging do?** It checks permission and request status, adds the proposed paper once, closes the request as MERGED, and records the review and merge in history.

**How did you verify it?** Core regression tests exercise permissions, copying, review outcomes, limits and persistence. The GUI smoke test exercises account creation, login, navigation, search, forking, theme switching and registration after logout.

**What would you improve next?** A database, verified institutional roles, real document upload, paper revision history and a server for concurrent users.

## Be precise about the limits

Call it an academic research collaboration prototype. Avoid claiming that it offers full Git version control or production-grade institutional security. This version stores paper text and runs locally on one computer.
