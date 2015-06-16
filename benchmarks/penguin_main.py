"""
Benchmark: ipython interface -> spark(spark SQL) -> Database : Profiling
Author: mira67, qliu.hit@gmail.com
Version: June, 2015
Function: 
1. Support database/query configuration
2. Time/Memory Profiling
3. Deliver query results: for post accuracy comparision vs other database
4. Auto-generated
5. Aggregate history query results into report --- Todo

How to use the script?
./bin/spark-submit /Users/mira67/Downloads/penguin_bm1.py --driver-class-path /Users/mira67/Documents/h2/bin/h2*.jar

"""
#Libraries
import sys
import os
from pyspark import SparkContext
from pyspark.sql import *
from math import floor
from time_profile import Timer
import matplotlib.pyplot as plt
import numpy as np
import database_cfg as dbcfg
import query_cfg as qcfg
import psutil

#Main Class
class PenguinBM(object):

    def __init__(self):
        self.dbType = "ssmi"
        self.dbName = "CH37H"
        self.tbName =["CH37H" ,"LOCMAP_S"]
        self.sqlStmPre = ""
        self.sqlStm = ""
        self.url = "jdbc:h2:tcp://localhost/~/ssmi9095ct"

    def memory_usage_psutil(self):
        # return the memory usage in MB
        process = psutil.Process(os.getpid())
        mem = process.get_memory_info()[0] / float(2 ** 20)
        return mem

    def setup(self):
        """
        1. database option
        2. query option: give a list
        3. temporal/spatial range: any (which days, which region)
        4. pixel requirements: range
        """
        #database info
        cfgdb = dbcfg.configDB()
        self.dbType = cfgdb.whichDB() #db[0], dbName

        #usr input query
        querycfg = qcfg.configQuery()
        querycfg.whichQuery()
        self.sqlStmPre = querycfg.queryStm()
        #construct full sql statement
        self.sqlStm = "SELECT * FROM %s t1, %s t2 " %(self.tbName[0], self.tbName[1])
        self.sqlStm += self.sqlStmPre
        print 'Generated SQL statement based on user input--> %s\n' % self.sqlStm

    def bmRun(self):
        """
        Connect DB from Spark and Run/Profile Query
        """
        #create output file for results
        print "Create benchmark output file for recoring..."
        file_out = open("/Users/mira67/Downloads/benchmark_output.txt", "w")
        print "start query evaluation, load tables from DB and register tables in Spark..."

        #load data with Spark
        with Timer() as tm:
            sc = SparkContext("local","penguin")
            #sc = SparkContext(master=local[2])
            sqlContext = SQLContext(sc)
             
            #queries test here, depends on queries to load table in memory
            df1 =sqlContext.load(source="jdbc",url=self.url, dbtable = self.tbName[0]) #dbtable is variable
            df1.registerTempTable(self.tbName[0]);

            df2 =sqlContext.load(source="jdbc",url=self.url, dbtable = self.tbName[1]) #dbtable is variable
            df2.registerTempTable(self.tbName[1]);

        mem_use = self.memory_usage_psutil()
        print "memory_use_load %s" %mem_use
        print "=> elasped load data: %s ms" % (tm.secs * 1000)

        #Query with Spark
        with Timer() as tm:
            #query
            rdf = sqlContext.sql(self.sqlStm)
            #hist of BT values
            #Todo
        mem_use = self.memory_usage_psutil()
        print "memory_use_load %s" %mem_use
        print "=> elasped query 1: %s ms" % (tm.secs * 1000)

        file_out.write("Query Time %s Memory %s\n" % (str(tm.secs * 1000),str(mem_use))) 
        #stop sparkcontext
        sc.stop()

        #Process with Spark - Todo

#setup and execute
bm = PenguinBM()
bm.setup()
bm.bmRun()



