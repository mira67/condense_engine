"""
Query configuration class for penguin benchmark
Todo: need construct a functiont to generate any query
"""
import sys
import os

#Main Class
class configQuery(object):

    def __init__(self):
        self.queryMode = {'1': 'temporal', '2': 'spatial', '3': 'combo'}
        self.queryRes = {}

    def whichQuery(self):
        """
        1. query option: spatial/tamporal
        """
        #ask user to select, and return name and driver name
        
        usrinput = raw_input('How would you like evaluate the database: temporal-1/spatial-2/combo-3 --> ')
        qm = self.queryMode[usrinput]
        self.queryRes['mode'] = qm
        if qm == 'temporal':
            #continuous range or dis? all months or some?
            temp_mode = raw_input('Continuous Time Range: con or dis --->')
            self.queryRes['temp_mode'] = temp_mode
            if self.queryRes['temp_mode'] == 'con':
                start_d = raw_input('Temporal Range Start yyyy-mm-dd--> ')
                self.queryRes['temp_start'] = start_d
                end_d = raw_input('Temporal Range End yyyy-mm-dd--> ')
                self.queryRes['temp_end'] = end_d
            else:
                yrs = raw_input('Selected Years by space splited 2002 2003 ...-->')
                self.queryRes['yrs'] = map(int, yrs.split())

            month = raw_input('Which months you like query all, 01/02,...--> ')
            self.queryRes['months'] = month #all or any other

            #pixel range, given options-todo
            pixel_range = raw_input('Input pixel range p1, p2--> ')
            self.queryRes['pixel_range'] = map(int, pixel_range.split())


        elif qm == 'spatial':
            #need add map size (rows, cols)
            region = raw_input('Spatial Region row range r1 r2--> ')
            self.queryRes['row_range'] =  map(int, region.split())
            region = raw_input('Spatial Region col range c1 c2--> ')
            self.queryRes['col_range'] =  map(int, region.split())

            #pixel range, given options-todo
            pixel_range = raw_input('Input pixel range p1, p2--> ')
            self.queryRes['pixel_range'] = map(int, pixel_range.split())


        elif qm == 'combo':
            #need add temporal range option
            if temp_mode == 'con':
                start_d = raw_input('Temporal Range Start yyyy-mm-dd--> ')
                self.queryRes['temp_start'] = start_d
                end_d = raw_input('Temporal Range End yyyy-mm-dd--> ')
                self.queryRes['temp_end'] = end_d
            else:
                yrs = raw_input('Selected Years by space splited 2002 2003 ...-->')
                self.queryRes['yrs'] = map(int, yrs.split())            
            
            month = raw_input('Which months you like query all, June/July,...--> ')
            self.queryRes['months'] = month #all or any other

            #need add map size (rows, cols)
            region = raw_input('Spatial Region row range r1 r2--> ')
            self.queryRes['row_range'] = map(int, region.split())
            region = raw_input('Spatial Region col range c1 c2--> ')
            self.queryRes['col_range'] = map(int, region.split())

            #pixel range, given options-todo
            pixel_range = raw_input('Input pixel range p1, p2--> ')
            self.queryRes['pixel_range'] = map(int, pixel_range.split())
        else:
            print 'Invalid input, please input option number\n'

    def queryStm(self):
        md = self.queryRes['mode']
        sqlStm = ""
        header = "WHERE "

        if md == 'temporal':
            #how to query month and random a list of years? #need a function for dis query
            #WHERE MONTH(t1.DATE) = %s
            if self.queryRes['temp_mode'] == 'con':
                sqlStm1 = "t1.DATE BETWEEN '%s' AND '%s' \
                AND t1.BT>%s \
                AND t1.BT<%s" %(self.queryRes['temp_start'],self.queryRes['temp_end'],\
                self.queryRes['pixel_range'][0],self.queryRes['pixel_range'][1])
            else:#discontinuous time range
                sqlStm1 = "t1.BT>%s AND t1.BT<%s" %(self.queryRes['pixel_range'][0],self.queryRes['pixel_range'][1])
                sqlStm2 = " AND YEAR(t1.DATE) = '%s'" % (self.queryRes['yrs'][0])
                yrlen = len(self.queryRes['yrs'])
                if yrlen > 1:
                    for yr in range(0,yrlen):
                        if yr > 0:
                            sqlStm2 += " OR YEAR(t1.DATE) = '%s'" %(self.queryRes['yrs'][yr])

            if self.queryRes['months'] == 'all':
                sqlStm3 = ""
            else:
                sqlStm3 = " AND MONTH(t1.DATE) = '%s'" % (self.queryRes['months'])

            #construct sql statement
            sqlStm = header + sqlStm1 + sqlStm2 + sqlStm3
                

        elif md == 'spatial':
            sqlStm = "WHERE t1.LOCID = t2.ID \
            AND t2.ROW < %s AND t2.ROW > %s \
            AND t2.COL < %s AND t2.COL > %s \
            AND t1.BT>%s \
            AND t1.BT<%s" %(self.queryRes['row_range'][1],self.queryRes['row_range'][0],self.queryRes['col_range'][1],self.queryRes['col_range'][0],self.queryRes['pixel_range'][0],self.queryRes['pixel_range'][1])

        elif md == 'combo':
            sqlStm = "WHERE t1.LOCID = t2.ID \
            AND t1.DATE BETWEEN '%s' AND '%s' \
            AND t2.ROW < %s AND t2.ROW > %s \
            AND t2.COL < %s AND t2.COL > %s \
            AND t1.BT>%s \
            AND t1.BT<%s" %(self.queryRes['temp_start'],self.queryRes['temp_end'],self.queryRes['row_range'][1],self.queryRes['row_range'][0],self.queryRes['col_range'][1],self.queryRes['col_range'][0],self.queryRes['pixel_range'][0],self.queryRes['pixel_range'][1])

        else:
            print 'Unrecognized mode\n'

        print sqlStm
        return sqlStm

#cfgdb = configQuery()
#cfgdb.whichQuery()
#db  =cfgdb.queryStm()


