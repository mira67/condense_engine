"""
Plotting APIs
"""
from IPython.display import display
from plotly.graph_objs import Bar, Scatter, Marker, Layout 
import plotly.plotly as py
from plotly.graph_objs import *
import pandas as pd
import requests
from plotly.graph_objs import *
import plotly.tools as tls
import numpy as np
from pyspark.sql import *

#sample plot
#Main Class
class queryPlot(object):
    def __init__(self):
        #py.sign_in("mira67", "oom4099r65")
        pass

    def qPlot(self,df):
        #histogram of queried data
        data = Data([Histogram(x=df.toPandas()['BT'])])
        py.iplot(data, filename="ssmi plot")

    def qMapDemo(self,df):
        #plot the surface map data from 1 day as example before user enter anything
        print "Example visulization of ssmi dataset"
        day1p = df.toPandas()
        print day1p.tail()
        z = day1p.BT.as_matrix()
        rown = 316
        coln = 332
        z = z.reshape(coln, rown)
        x = np.arange(1,316,1)
        y = np.arange(1,331,1)

        #example dataset
        trace1 = Surface(
            z=z,
            x=x,
            y=y,
        )
        data = Data([trace1])
        # Dictionary of style options for all axes
        axis = dict(
            showbackground=True, # (!) show axis background
            backgroundcolor="rgb(204, 204, 204)", # set background color to grey
            gridcolor="rgb(255, 255, 255)",       # set grid line color
            zerolinecolor="rgb(255, 255, 255)",   # set zero grid line color
        )

        # Make a layout object
        layout = Layout(
            title='Mapping Pixels in 2D', # set plot title
            scene=Scene(  # (!) axes are part of a 'scene' in 3d plots
                xaxis=XAxis(axis), # set x-axis style
                yaxis=YAxis(axis), # set y-axis style
                zaxis=ZAxis(axis)  # set z-axis style
            )
        )
        # Make a figure object
        fig = Figure(data=data, layout=layout)
        # (@) Send to Plotly and show in notebook
        tls.embed("https://plot.ly/~mira67/82/mapping-pixels-in-2d/")
        #py.iplot(fig, filename='Pixels Mapping Example')
        print "Plot completed"

