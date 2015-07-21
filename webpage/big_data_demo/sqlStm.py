"""
Query configuration class for penguin benchmark
Todo: need construct a functiont to generate any query
"""
import sys
import os

#Main Class
class configQuery(object):

    def queryStm(self,mode,start_date,end_date,start_row,end_row,start_col,end_col):
        md = self.queryRes['mode']
        sqlStm = ""

        sqlStm = "WHERE t1.LOCID = t2.ID \
        AND t1.DATE BETWEEN '%s' AND '%s' \
        AND t2.ROW < %s AND t2.ROW > %s \
        AND t2.COL < %s AND t2.COL > %s \
        AND t1.BT>%s \
        AND t1.BT<%s" %(self.queryRes['temp_start'],self.queryRes['temp_end'],self.queryRes['row_range'][1],self.queryRes['row_range'][0],self.queryRes['col_range'][1],self.queryRes['col_range'][0],self.queryRes['pixel_range'][0],self.queryRes['pixel_range'][1])
        return sqlStm



