# Upload to GitHub from VS Code

1. Extract the ZIP. Open the inner CUTResearchHub folder (the one containing src and README.md) in VS Code.
2. Ensure Git is installed and available to VS Code.
3. Open Source Control and select Initialize Repository.
4. Review the changed files. The supplied .gitignore excludes hub_data, backups, build output, JARs and IDE settings.
5. Stage the source files and documentation. Enter a commit message such as Initial CUT Research Hub project and commit.
6. Select Publish to GitHub, sign in if prompted, and choose Publish to GitHub private repository.
7. Open the repository on GitHub and verify that src, test, README.md and build/run scripts are present.

Do not upload the ZIP as your only repository file. Publish the extracted project so the Java source is visible. Website drag-and-drop uploads do not apply .gitignore rules automatically.

If you have already committed local data, .gitignore does not untrack it. Stop and remove it from tracking before pushing.

The generated JAR is excluded from Git; users cloning the repository need JDK 17+ and should run build.bat before run.bat. The downloadable project ZIP still includes a ready-built JAR.

For an existing GitHub repository, use its URL rather than creating a second repository.
