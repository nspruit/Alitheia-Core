package eu.sqooss.impl.service.alitheia;


/**
* eu/sqooss/impl/service/alitheia/LoggerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from Alitheia.idl
* den 14 december 2007 kl 14:25 CET
*/

public interface LoggerOperations 
{
  void debug (String logger, String text);
  void info (String logger, String text);
  void warn (String logger, String text);
  void error (String logger, String text);
} // interface LoggerOperations
