# Assignment 2 - Testing and Refactoring Report

Intro

## Chosen violation
What are the violations and which classes/packages are involved in the violation? Why is it a shortcoming? (See previous report+include numbers). Why do we want to fix this?

## Testing
Now that we have chosen to tackle the violations in the DBService(Impl) class, we have to make sure our refactorings are enabled by tests. To do this, we have first collected the test coverage data using the JaCoCo plugin in Maven. Then we have added new tests to incrementally increase the coverage for the public methods in DBService(Impl), as these methods are called by methods in other classes and need therefore be tested well. These two processes as well as the test coverage data after all tests have been written are presented in the following three sections.

### Coverage before
Show coverage image and explain

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



