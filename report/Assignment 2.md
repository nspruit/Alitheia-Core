# Assignment 2 - Testing and Refactoring Report

Intro

## Chosen violations
In our Reverse Engineering and Problem Detection Report we explained that de `DBService` interface is an example of a violation of the Open-Closed Principle as it strongly depends on Hibernate, especially in its `doHQL` methods. So, if the decision is made to switch to an alternative for Hibernate in the future, this interface and its implementation(s) need to be changed instead of extended. This means that many classes that depend on this interface need to be modified as well, which has a high probability of introducing new bugs which in turn increases the maintenance cost significantly.

Secondly, the `DBService` interface and therefore also its implementation, `DBServiceImpl`, also violate the Single Responsibility Principle. The `DBService` interface violates the SRP as it has more than one responsibility, namely managing database sessions, storing, retrieving and reading database objects and executing custom SQL and HQL queries. This means that when the functionality of one of these responsibilities needs to be changed, the `DBService` interface and/or its implementation need to be changed. 

Fixing the mentioned violations in this interface and its implementation will significantly reduce maintenance cost, as many classes will have to be changed less often after the refactorings, as there are many dependencies on `DBService`. The fact that `DBServiceImpl` is also a large (630 lines of code) and complex (average McCabe complexity of 5.2) class in addition to the reasons mentioned above, make the `DBService` interface and the `DBServiceImpl` class good candicates for refactoring. That is why we have decided to tackle the problems in these classes.

## Testing
Now that we have chosen to tackle the violations in the `DBServiceImpl` class, we have to make sure our refactorings are enabled by tests. To do this, we have first collected the test coverage data using the [JaCoCo](http://www.eclemma.org/jacoco/) plugin in Maven. Then we have added new tests to incrementally increase the coverage for the public methods in `DBServiceImpl`, as these methods are called by methods in other classes and need therefore be tested well. These two processes as well as the test coverage data after all tests have been written are presented in the following three sections.

### Coverage before
Running the tests with Maven resulted in an HTML coverage report containing all classes in Alitheia-Core. As our refactorings are only concerned with the `DBServiceImpl` class, the coverage data for `DBServiceImpl` is most useful. The coverage data for this class is shown in the following table for all the implemented methods of the `DBService` interface (a value of n/a indicates that the method only has one branch).

| Method                                                                  | Line coverage (%) | Branch coverage (%) |
| ----------------------------------------------------------------------- | ----------------- | ------------------- |
| getInstance                                                             | 0                 | 0                   |
| findObjectById                                                          | 0                 | n/a                 |
| findObjectByIdForUpdate                                                 | 0                 | n/a                 | 
| findObjectsByProperties                                                 | 0                 | n/a                 |
| findObjectsByPropertiesForUpdate                                        | 0                 | n/a                 |
| doSQL(String)                                                           | 0                 | n/a                 |
| doSQL(String,Map<String,Object>)                                        | 0                 | 0                   |
| callProcedure                                                           | 0                 | 0                   |
| doHQL(String)                                                           | 0                 | n/a                 |
| doHQL(String,Map<String,Object>)                                        | 0                 | n/a                 |
| doHQL(String,Map<String,Object>,int)                                    | 0                 | n/a                 |
| doHQL(String,Map<String,Object>,boolean)                                | 0                 | n/a                 |
| doHQL(String,Map<String,Object>,Map<String,Collection>)                 | 0                 | n/a                 |
| doHQL(String,Map<String,Object>,Map<String,Collection>,boolean,int,int) | 0                 | 0                   |
| addRecord                                                               | 0                 | n/a                 |
| deleteRecord                                                            | 0                 | n/a                 |
| addRecords                                                              | 0                 | 0                   |
| deleteRecords                                                           | 0                 | 0                   |
| logger                                                                  | 0                 | n/a                 |
| startDBSession                                                          | 0                 | 0                   |
| commitDBSession                                                         | 0                 | 0                   |
| rollbackDBSession                                                       | 0                 | 0                   |
| flushDBSession                                                          | 0                 | 0                   |
| isDBSessionActive                                                       | 0                 | 0                   |
| attachObjectToDBSession                                                 | 0                 | 0                   |
| executeUpdate                                                           | 0                 | 0                   |

As can be seen from the table, none of the implementations of the methods from the `DBService` interface are tested at all. Therefore we cannot just start refactoring the code, as this will likely introduce new bugs, which will only increase the future maintenance cost instead of reducing it. Therefore it was necessary to first test all these methods by writing unit tests for them. How this was done is explained in the next section.

### Writing tests
What did we test, why and how? Explain InMemoryDB, mocking in unit tests, integration tests, etc.

### Coverage after
After all tests have been written the test coverage data for the implementations of methods of the `DBService` interface  was again obtained using JaCoCo. The results are shown in the table below.

| Method                                                                  | Line coverage (%) | Branch coverage (%) |
| ----------------------------------------------------------------------- | ----------------- | ------------------- |
| getInstance                                                             | 0                 | 0                   |
| findObjectById                                                          | 100               | n/a                 |
| findObjectByIdForUpdate                                                 | 100               | n/a                 | 
| findObjectsByProperties                                                 | 100               | n/a                 |
| findObjectsByPropertiesForUpdate                                        | 100               | n/a                 |
| doSQL(String)                                                           | 0                 | n/a                 |
| doSQL(String,Map<String,Object>)                                        | 0                 | 0                   |
| callProcedure                                                           | 0                 | 0                   |
| doHQL(String)                                                           | 100               | n/a                 |
| doHQL(String,Map<String,Object>)                                        | 100               | n/a                 |
| doHQL(String,Map<String,Object>,int)                                    | 100               | n/a                 |
| doHQL(String,Map<String,Object>,boolean)                                | 100               | n/a                 |
| doHQL(String,Map<String,Object>,Map<String,Collection>)                 | 100               | n/a                 |
| doHQL(String,Map<String,Object>,Map<String,Collection>,boolean,int,int) | 81                | 94                  |
| addRecord                                                               | 100               | n/a                 |
| deleteRecord                                                            | 100               | n/a                 |
| addRecords                                                              | 52                | 67                  |
| deleteRecords                                                           | 51                | 67                  |
| logger                                                                  | 0                 | n/a                 |
| startDBSession                                                          | 98                | 83                  |
| commitDBSession                                                         | 86                | 75                  |
| rollbackDBSession                                                       | 97                | 75                  |
| flushDBSession                                                          | 97                | 75                  |
| isDBSessionActive                                                       | 98                | 88                  |
| attachObjectToDBSession                                                 | 77                | 100                 |
| executeUpdate                                                           | 60                | 83                  |

The table shows that the written tests in general increased the line and branch coverage significantly. Most methods have a line coverage of over 80 percent, which is quite high considering the fact that the other lines mostly deal with handling exceptions caused by errors in the database. For the methods that contain multiple branches, the branch coverage is in general higher than 67%. This percentage is also quite high and the missed branches are again mostly located in the exception handling code dealing with database errors.

The coverage for five of the methods has not changed as we simply have not tested them. We have decided not to test the `getInstance` and `logger` methods because these methods are simple getters. We have also decided not to write tests for the two `doSQL` methods and the `callProcedure` method, as these methods are deprecated. Moreover, we have also checked the 'Call Hierarchy' of these methods in Eclipse to see whether the methods are actually called by other classes and this is not the case. Therefore we believe it is not worth the effort to write tests for these methods.

## Refactoring
Now that the test harness is in place, the actual refactorings to the `DBService` interface and its implementation in `DBServiceImpl` could safely be done. For this we have first created a new design, then we have updated the tests according to this new design and then the refactorings were implemented. These processes will be examined in the following three sections.

### Suggested design
Show UML diagram and explain. Also explain why this solves the violations, has a low probability of introducing new bugs, etc. 

### Updating the tests
Explain how we updated the written tests according to the new design. Explain this separately for each of the following test classes:

* DBObject
* DBTransactionTest
* HQLQueryInterfaceTest
* HQLQueryInterfaceImplTest
* InMemoryDatabase
* QueryInterfaceTest
* ConfigurationOptionTest
* MetricTest

### Implementing the new design
How did we create the new design/updated the old classes (by incrementally getting more tests to pass)? Also indicate this for each of the following actions:

* Create DBSessionManager
* Create DBSessionManagerImpl
* Create QueryInterface, HQLQueryInterface and SQLQueryInterface
* Create HQLQueryInterfaceImpl
* Create SQLQueryInterfaceImpl 
* Update DBService
* Update DBServiceImpl
* Create DBSessionValidation to get rid of duplicate logging code
* Also: Update calls to session management methods
* Also: Update calls to query functions

## Results
What are the results of the refactoring? Are the violations gone and why? Try to include hard numbers here (complexity?,LCOM?). Why is this a good thing for future maintenance? Also explain that and why we are convinced that we haven't introduced new bugs and the system still works the same as before.

## Conclusion
Is this section necessary? Maybe merge with Results section.



