# Assignment 2 - Testing and Refactoring Report

Intro

## Chosen violation
What are the violations and which classes/packages are involved in the violation? Why is it a shortcoming? (See previous report+include numbers). Why do we want to fix this?

## Testing
Now that we have chosen to tackle the violations in the DBService(Impl) class, we have to make sure our refactorings are enabled by tests. To do this, we have first collected the test coverage data using the JaCoCo plugin in Maven. Then we have added new tests to incrementally increase the coverage for the public methods in DBService(Impl), as these methods are called by methods in other classes and need therefore be tested well. These two processes as well as the test coverage data after all tests have been written are presented in the following three sections.

### Coverage before
Running the tests with Maven resulted in an HTML coverage report containing all classes in Alitheia-Core. As our refactorings are only concerned with the DBService(Impl) class, the coverage data for DBServiceImpl is most useful. The coverage data for this class is shown in the following table for all the implemented methods of the DBService interface (a value of n/a indicates that the method only has one branch).

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

As can be seen from the table, none of the implementations of the methods from the DBService interface are tested at all. Therefore we cannot just start refactoring the code, as this will likely introduce new bugs, which will only increase the future maintenance cost instead of reducing it. Therefore it was necessary to first test all these methods by writing unit tests for them. How this was done is explained in the next section.

### Writing tests
What did we test, why and how? Explain InMemoryDB, mocking in unit tests, integration tests, etc.

### Coverage after
Show coverage image after the tests have been written. Also explain that the tests are sufficient for the refactoring.

## Refactoring
Intro

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
What are the results of the refactoring? Are the violations gone and why? Try to include hard numbers here. Why is this a good thing for future maintenance? Also explain that and why we are convinced that we haven't introduced new bugs and the system still works the same as before.

## Conclusion
Is this section necessary? Maybe merge with Results section.



