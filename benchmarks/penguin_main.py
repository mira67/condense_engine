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
./bin/spark-submit /Users/mira67/Downloads/penguin_main.py --driver-class-path /Users/mira67/Documents/h2/bin/h2*.jar
use with Spark 1.4.0

"""
#Libraries
import sys
import os
from pyspark import SparkContext
from pyspark.sql import *
from pyspark.sql.types import StringType
from math import floor
from time_profile import Timer
import matplotlib.pyplot as plt
import numpy as np
import database_cfg as dbcfg
import query_cfg as qcfg
import psutil
import query_plot as qplt

#Main Class
class PenguinBM(object):

    def __init__(self):
        self.dbType = "ssmi"
        self.dbName = "CH37H"
        self.tbName =["CH37H" ,"LOCMAP_S"]
        self.mtb = ["ssmi","map"]
        self.sqlStmPre = ""
        self.sqlStm = ""
        self.url = "jdbc:h2:tcp://localhost/~/ssmiSmall"

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
        self.sqlStm = "SELECT * FROM %s t1, %s t2 " %(self.mtb[0], self.mtb[1])
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
            df1 =sqlContext.read.jdbc(url=self.url, table = self.tbName[0],lowerBound = 0, upperBound = 350, numPartitions=200)#dbtable is variable
            df1.registerTempTable(self.tbName[0])

            df2 =sqlContext.read.jdbc(url=self.url, table = self.tbName[1],lowerBound = 0, upperBound = 350, numPartitions=200)#dbtable is variable
            df2.registerTempTable(self.tbName[1])

            #register helper functions for SQL
            sqlContext.registerFunction("MONTH", lambda x: x[5:7], StringType())#grab Month
            sqlContext.registerFunction("YEAR", lambda x: x[0:4], StringType())
            sqlContext.registerFunction("DAY", lambda x: x[8:10], StringType())

            rdf1 = sqlContext.sql("SELECT * FROM "+self.tbName[0])
            rdf2 = sqlContext.sql("SELECT * FROM " + self.tbName[1])
            sqlContext.registerDataFrameAsTable(rdf1, self.mtb[0])
            sqlContext.registerDataFrameAsTable(rdf2, self.mtb[1])

        mem_use = self.memory_usage_psutil()
        print "memory_use_load %s" %mem_use
        print "=> elasped load data: %s ms" % (tm.secs * 1000)

        #Query with Spark
        with Timer() as tm:
            #query
            rdf = sqlContext.sql(self.sqlStm)
#need register as table first
            print "Data schema from query:"
            rdf.printSchema()
            #hist of BT values
            #Todo
        mem_use = self.memory_usage_psutil()
        print "memory_use_load %s" %mem_use
        print "=> elasped: %s ms" % (tm.secs * 1000)

        file_out.write("Query Time %s Memory %s\n" % (str(tm.secs * 1000),str(mem_use))) 
                
        #example enabled
        day1 = sqlContext.sql("SELECT * FROM ssmi t1, map t2 WHERE t1.DATE BETWEEN '1990-01-01' AND '1990-01-01' AND t1.LOCID = t2.ID ORDER BY t1.LOCID")
        #call plot
        demoplt = qplt.queryPlot()
        demoplt.qMapDemo(day1)

        
        #stop sparkcontext
        sc.stop()

        #Process with Spark - Todo

#setup and execute
bm = PenguinBM()
bm.setup()
bm.bmRun()



