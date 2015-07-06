#!/usr/bin/env python
from __future__ import print_function
# -*- coding: UTF-8 -*-# enable debugging
import cgitb

import datetime
import sys
sys.path.insert(0, "/opt/spark-1.4.0/bin/pyspark") # for importing dependencies https://docs.python.org/2/library/cgi.html#installing-your-cgi-script-on-a-unix-system

from random import random
from operator import add
"""
from pyspark import SparkContext

if __name__ == "__main__":
    sc = SparkContext(appName="PythonPi")
    partitions = int(sys.argv[1]) if len(sys.argv) > 1 else 2
    n = 100000 * partitions

    def f(_):
        x = random() * 2 - 1
        y = random() * 2 - 1
        return 1 if x ** 2 + y ** 2 < 1 else 0

    count = sc.parallelize(range(1, n + 1), partitions).map(f).reduce(add)
    #print("Pi is roughly %f" % (4.0 * count / n))

    sc.stop()

"""
cgitb.enable()
print("Content-Type: text/html;charset=utf-8\n")
print(datetime.datetime.now())
"""
print("World Pyspark")
print("Pi is roughly %f" % (4.0 * count / n))
print("Hello World Pyspark")
"""
