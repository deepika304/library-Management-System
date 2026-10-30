# Library Management System (Java + JDBC + MySQL)

## 1. Setup
1. Install JDK 11+ and MySQL 8.
2. Run the database script:   mysql -u root -p < sql/library_db.sql
3. Put mysql-connector-j-8.x.x.jar inside lib/
4. Edit db.properties (db.url, db.user, db.password)

## 2. Compile (run from the project root folder)
Linux / macOS:
    javac -cp "lib/*" -d out $(find src -name "*.java")
Windows (Command Prompt):
    dir /s /b src\*.java > sources.txt
    javac -cp "lib/*" -d out @sources.txt

## 3. Run (must be run from the project root so db.properties is found)
Linux / macOS:   java -cp "out:lib/*" com.library.Main
Windows:         java -cp "out;lib/*" com.library.Main

## IntelliJ / Eclipse
Create a Java project, mark src as the source folder, add the connector jar as a library,
and set the working directory to the project root. Run com.library.Main.
