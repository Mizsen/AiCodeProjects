
set JAVA_HOME="E:\Windows10\Java64\jdk-21"
set path=E:\Windows10\Java64\jdk-21\bin;E:\__apache\apache-maven-3.9.10\bin;%path%

mvn clean package

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dfile.encoding=GBK"

java -Dfile.encoding=GBK -jar your-app.jar


chcp 65001








# Prescription Management System - Backend

## Overview
The Prescription Management System is designed to provide a comprehensive solution for managing prescriptions and drug information in healthcare institutions. This backend module is built using Spring Boot and MyBatis, ensuring a robust and scalable architecture.

## Project Structure
- **src/main/java/com/example/prescription**: Contains the main application code.
  - **config**: Configuration classes for security and database settings.
  - **controller**: REST controllers for handling API requests.
  - **entity**: Entity classes representing database tables.
  - **mapper**: MyBatis mapper interfaces for database operations.
  - **service**: Service classes implementing business logic.
  - **security**: Security-related classes for JWT authentication and role-based access control.
- **src/main/resources**: Contains configuration files and SQL mapper XML files.
- **pom.xml**: Maven configuration file for managing dependencies and plugins.

## Getting Started

### Prerequisites
- Java 21  springboot3
- Maven

### Installation
1. Clone the repository:
   ```
   git clone <repository-url>
   ```
2. Navigate to the backend directory:
   ```
   cd prescription-management-system/backend
   ```
3. Build the project:
   ```
   mvn clean install
   ```

### Running the Application
To run the application, execute the following command:
```
mvn spring-boot:run
```

### API Documentation
Refer to the controller classes for detailed API endpoints and their usage.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License
This project is licensed under the MIT License. See the LICENSE file for details.



        <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <!-- 解决JDK 9+的模块路径问题 -->
                    <compilerArgs>
                        <arg>--add-modules</arg>
                        <arg>java.se</arg>
                        <arg>--add-exports</arg>
                        <arg>java.base/jdk.internal.ref=ALL-UNNAMED</arg>
                        <arg>--add-exports</arg>
                        <arg>java.base/java.lang=ALL-UNNAMED</arg>
                    </compilerArgs>
                </configuration>
            </plugin>  




mvn install -DskipTests
mvn test
mvn test -Dtest=UserControllerTest
mvn test -Dtest=com.example.prescription.controller.UserControllerTest
mvn test -Dtest=UserControllerTest#login_WithValidCredentials_ShouldReturnToken


mvn test -Dtest=UserControllerTest#register_ShouldReturnSuccessMessage


-- 这个是api接口。
mvn exec:java -Dexec.mainClass="test.ApiTest" -Dexec.classpathScope=test