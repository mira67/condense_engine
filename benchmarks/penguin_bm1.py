"""
Benchmark Evalution Script: python for spark and query

Author: mira67, qliu.hit@gmail.com

How to use the script?
./bin/spark-submit /Users/mira67/Downloads/penguin_bm1.py --driver-class-path /Users/mira67/Documents/h2/bin/h2*.jar
"""

"""
Usage-create realistic testing cases
auto: auto-random cases testing mode
manual: use input testing mode
"""

import sys
import os
from pyspark import SparkContext
from pyspark.sql import SQLContext
from math import floor
from time_profile import Timer
import matplotlib.pyplot as plt
import numpy as np

#todo - input arguments as DB meta data
tbName = "CH19V"
temporal_size = [1990, 2014]#1yr, 2yr, 4yr, 8yr, 16yr,25yr
temporal_range = [1, 2, 4, 8, 16, 25]
row_sz = 316
col_sz  =332
spatial_size = {'rows':row_sz, 'cols':col_sz}
spatial_range = [1,0.5,0.25]
thr1 = 150
thr2 = 250
tbMap = "LOCMAP_S"

#output metrics related parameters
publish_resuls = True
query1_out = [[],[]]  
query2_out = [[],[],[]]

mem_use = 0 #Mb
#quick memory usage evaluation, need check how it measure the memory usage?
def memory_usage_psutil():
    # return the memory usage in MB
    import psutil
    process = psutil.Process(os.getpid())
    mem = process.get_memory_info()[0] / float(2 ** 20)
    return mem

if __name__ == "__main__":

    db = raw_input('Which database? --> ')
    ch = raw_input('Which channels? i.e. 19v, 19h --> ')
    autoquery = raw_input('Auto temporal/spatial tests, True or False? --> ')
    print "Database: %s, Channels: %s, AutoqueryTest: %s" % (db, str(ch),str(autoquery))

    """
    Usage: 
    """
    print "Create benchmark output file for recoring..."
    file_out = open("/Users/mira67/Downloads/benchmark_output.txt", "w")
    print "start query evaluation, load tables from DB and register tables in Spark..."
    
    with Timer() as tm:
        sc = SparkContext(appName="Benchmark1")
        sqlContext = SQLContext(sc)
    
        #queries test here, depends on queries to load table in memory
        df1 =sqlContext.load(source="jdbc",driver="org.h2.Driver",url="jdbc:h2:tcp://localhost/~/ssmi?user=sa&password=1234", dbtable = tbName) #dbtable is variable
        df1.registerTempTable(tbName);

        df2 =sqlContext.load(source="jdbc",driver="org.h2.Driver",url="jdbc:h2:tcp://localhost/~/ssmi?user=sa&password=1234", dbtable = tbMap) #dbtable is variable
        df2.registerTempTable(tbMap);

    mem_use = memory_usage_psutil()
    print "memory_use_load %s" %mem_use
    print "=> elasped load data: %s ms" % (tm.secs * 1000)
   
    for trg in temporal_range:
        print("Current Temporal Range %s  " % trg)
        startyr = temporal_size[0]
        endyr = startyr + trg - 1
        #create sql statement for trg
        sql_trg = " AND t1.DATE BETWEEN '%s-01-01' AND '%s-12-31'" %(startyr, endyr)
        print sql_trg

        with Timer() as tm:
            #query-1 return pixels between thr1-thr2
            rdf = sqlContext.sql("SELECT BT FROM " +tbName + " t1 WHERE BT>"+str(thr1) +" AND BT<"+str(thr2) + sql_trg)
            #hist of BT values
        mem_use = memory_usage_psutil()
        print "memory_use_load %s" %mem_use

        file_out.write("Query1  Temporal_Range  %s  Spatial_Range   %s  Time %s Memory  %s\n" % (str(trg), 'NA', str(tm.secs * 1000),str(mem_use))) 
        print "=> elasped query 1: %s ms" % (tm.secs * 1000)

        query1_out[0].append(tm.secs*1000)
        query1_out[1].append(mem_use)

        
        #query-2 return pixels at location range(all, half, 1 fourth)
        for srg in spatial_range:
            row_b = floor(row_sz * srg)
            col_b = floor(col_sz * srg)
            """
                SELECT *
                FROM CH91H t1 , LOCMAP_L t2
                WHERE t1.LOCID = t2.ID and t2.ROW = 1 and t2.COL = 1
            """

            with Timer() as tm:

                rdf = sqlContext.sql("SELECT BT FROM " +tbName +" t1," +tbMap +" t2 WHERE t1.LOCID = t2.ID and t2.ROW <" + str(row_b) + " AND t2.COL < " + str(col_b) + sql_trg)
            #hsit of BT values
            mem_use = memory_usage_psutil()
            print "memory_use_load %s" %mem_use
            file_out.write("Query2  Temporal_Range  %s  Spatial_Range   %s  Time %s Memory  %s\n" % (str(trg),str(srg),str(tm.secs * 1000),str(100))) 
            print "=> elasped query 2: %s ms" % (tm.secs * 1000)
            
            query2_out[0].append(srg)
            query2_out[1].append(tm.secs*1000)
            query2_out[2].append(mem_use)



    #todo-record performance - query-1 (load:x ms, mem: x bytes, compute:x ms, mem:x bytes, plot:x ms,...) 
    print "done with test, close file and sc"
    file_out.close()
    sc.stop()

    #publish results in plots
    if publish_resuls:
        #plot query1 results

        fig, ax1 = plt.subplots()
        ax1.plot(temporal_range, query1_out[0],'bo-')
        ax1.set_xlabel('Temporal Range (years)')
        # Make the y-axis label and tick labels match the line color.
        ax1.set_ylabel('Execution Time (ms)', color='b')
        for tl in ax1.get_yticklabels():
            tl.set_color('b')

        ax2 = ax1.twinx()
        ax2.plot(temporal_range, query1_out[1],'r*-')
        ax2.set_ylabel('Memory Usage (Mb)', color='r')
        for tl in ax2.get_yticklabels():
            tl.set_color('r')

        plt.title("Query 1 Performance")
        plt.show()
