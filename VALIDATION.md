# Validation

Validated using OpenJDK 17 on Linux, with a virtual X11 desktop for Swing.

- Sources compiled with `--release 17` and UTF-8 encoding.
- Executable JAR built with `App` as its main class.
- **36 core regression checks passed**, including a compatibility check against the original supplied `hub_data/hub.ser`.
- **11 desktop GUI checks passed**: role options, registration, login, repository listing, live search, correct filtered result, forking with copied papers, theme label, navigation, registration after sign-out, and researcher controls. (Some related actions share a single assertion.)
- Registration, dashboard, repository table, dark theme, and 1000 × 700 layout were visually inspected.
- Core tests also passed against the built JAR.

The ordinary test script runs 35 core checks; the extra original-data check was run separately using the supplied data path. Tests use temporary data and do not register accounts in the delivered application.

Windows batch scripts are included for the intended Windows workflow. They have been reviewed but were not executed on Windows in this environment. Native fonts and window decorations may differ.
