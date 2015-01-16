# Assignment 2 - Testing and Refactoring Report

Intro

## Chosen violations
In our Reverse Engineering and Problem Detection Report we explained that de DBService interface is an example of a violation of the Open-Closed Principle as it strongly depends on Hibernate, especially in its doHQL methods. So, if the decision is made to switch to an alternative for Hibernate in the future, this interface and its implementation(s) need to be changed instead of extended. This means that many classes that depend on this interface need to be modified as well, which has a high probability of introducing new bugs which in turn increases the maintenance cost significantly.

Secondly, the DBService interface and therefore also its implementation, DBServiceImpl, also violate the Single Responsibility Principle. The DBService interface violates the SRP as it has more than one responsibility, namely managing database sessions, storing, retrieving and reading database objects and executing custom SQL and HQL queries. This means that when the functionality of one of these responsibilities needs to be changed, the DBService interface and/or its implementation need to be changed. 

Fixing the mentioned violations in this interface and its implementation will significantly reduce maintenance cost, as many classes will have to be changed less often after the refactorings, as there are many dependencies on DBService. The fact that DBServiceImpl is also a large (630 lines of code) and complex (average McCabe complexity of 5.2) class in addition to the reasons mentioned above, make the DBService interface and the DBServiceImpl class good candicates for refactoring. That is why we have decided to tackle the problems in these classes.

## Testing
Intro

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
What are the results of the refactoring? Are the violations gone and why? Try to include hard numbers here (complexity?,LCOM?). Why is this a good thing for future maintenance? Also explain that and why we are convinced that we haven't introduced new bugs and the system still works the same as before.

## Conclusion
Is this section necessary? Maybe merge with Results section.



