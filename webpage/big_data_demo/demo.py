import os
import urllib

import jinja2
import webapp2
import webapp2static

import pprint
import logging
from pyspark import SparkContext
from pyspark.sql import *
from pyspark.sql.types import StringType
from time_profile import Timer
import pandas as pd

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

#setup spark and run test app
#start sparkcontext testing code, keep it here before release
logFile = "/opt/spark-1.4.0/README.md"  # Should be some file on your system
sc = SparkContext("local", "Demo App")
logData = sc.textFile(logFile).cache()
numAs = logData.filter(lambda s: 'a' in s).count()
numBs = logData.filter(lambda s: 'b' in s).count()
print "Spark is successfully running: Lines with a: %i, lines with b: %i" % (numAs, numBs)

#global variables
dbType = "ssmi"
dbName = "CH37H"
tbName =["CH37H" ,"LOCMAP_S"]
mtb = ["ssmi","map"]
url = "jdbc:h2:tcp://localhost:9292/~/ssmiSmall"
#url = "jdbc:h2:tcp://localhost:9292/home/mira67/ssmiSmall"

#web server 
class MainPage(webapp2.RequestHandler):

    def get(self):
        template_values ={}
        template = JINJA_ENVIRONMENT.get_template('index.html')
        self.response.write(template.render(template_values))

#construct query statement
class sqlStm(object):
    def __init__(self):
        self.sqlStmPre = ""
        self.sqlStm = ""

    def stm(self,start_date,end_date,start_row,end_row,start_col,end_col,pixel_lower,pixel_upper):
        """
        1. Construct sql statement for Spark-SQL from user inputs
        """
        #usr input query
        self.sqlStmPre = "WHERE t1.LOCID = t2.ID \
        AND t1.DATE BETWEEN '%s' AND '%s' \
        AND t2.ROW > %s AND t2.ROW < %s \
        AND t2.COL > %s AND t2.COL < %s \
        AND t1.BT>%s \
        AND t1.BT<%s" \
        %(start_date,end_date,start_row,end_row,start_col,end_col,pixel_lower,pixel_upper)
        #construct full sql statement
        self.sqlStm = "SELECT * FROM %s t1, %s t2 " %(mtb[0], mtb[1])
        self.sqlStm += self.sqlStmPre
        print 'Generated SQL statement based on user input--> %s\n' % self.sqlStm
        return self.sqlStm

#query
class Query(webapp2.RequestHandler):

    def post(self):
        ifquery = self.request.get('id')
        if ifquery == "Query":
            temporal = self.request.get('temporal')
            row = self.request.get('rowselect')
            col = self.request.get('colselect')
            pixel = self.request.get('pselect')
            #parse and send parameters to spark-SQL for analysis
            start_date = temporal[0:10]
            end_date = temporal[13:]
            print start_date,end_date
            row_opt = row.split(',')
            start_row = row_opt[0]
            end_row = row_opt[1]
            col_opt = col.split(',')
            start_col = col_opt[0]
            end_col = col_opt[1]
            print start_row,start_col
            pixel_val = pixel.split(',')
            pixel_lower = pixel_val[0]
            pixel_upper = pixel_val[1]
            print pixel_lower,pixel_upper

            sql = sqlStm()
            statement = sql.stm(start_date,end_date,start_row,end_row,start_col,end_col,pixel_lower,pixel_upper)
            #load data with Spark
            with Timer() as tm:
                sqlContext = SQLContext(sc)
                #queries test here, depends on queries to load table in memory
                df1 =sqlContext.read.jdbc(url=url, table = tbName[0],lowerBound = 0, upperBound = 350, numPartitions=200)#dbtable is variable
                df1.registerTempTable(tbName[0])

                df2 =sqlContext.read.jdbc(url=url, table = tbName[1],lowerBound = 0, upperBound = 350, numPartitions=200)#dbtable is variable
                df2.registerTempTable(tbName[1])

                #register helper functions for SQL
                sqlContext.registerFunction("MONTH", lambda x: x[5:7], StringType())#grab Month
                sqlContext.registerFunction("YEAR", lambda x: x[0:4], StringType())
                sqlContext.registerFunction("DAY", lambda x: x[8:10], StringType())

                rdf1 = sqlContext.sql("SELECT * FROM "+ tbName[0])
                rdf2 = sqlContext.sql("SELECT * FROM " + tbName[1])
                sqlContext.registerDataFrameAsTable(rdf1, mtb[0])
                sqlContext.registerDataFrameAsTable(rdf2, mtb[1])

            print "=> elasped load data: %s ms" % (tm.secs * 1000)
            #Query with Spark
            with Timer() as tm:
                #query
                rdf = sqlContext.sql(statement)
                #need register as table first
                print "Data schema from query:"
                rdf.printSchema()
            #get return back and set data for Histogram plot
            df = rdf.toPandas()
            data = df['BT'].values.tolist()
            #a list of pixel values
            #obj = [65, 59, 80, 81, 56, 55, 40]
            self.response.out.write(data)

app = webapp2.WSGIApplication([
    (r'/', MainPage),
    ('/user_query', Query),
    (r'/static/(.+)', webapp2static.StaticFileHandler)
], config = {'webapp2static.static_file_path': './bigdata_demo/static'})

# Create an app to serve static files
# Choose a directory separate from your source (e.g., "static/") so it isn't dl'able
#static_app = StaticURLParser("static/")

# Create a cascade that looks for static files first, then tries the webapp
#app2 = Cascade([static_app, web_app])
#Hi Rudy, the data source works now, no further changes :) Test it out!
def main():
    from paste import httpserver
    httpserver.serve(app, host='10.240.85.53', port='8989')

if __name__ == '__main__':
    main()
