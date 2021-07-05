import pandas as pd
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import scipy.stats as stats
import matplotlib.mlab as mlab
import math

# Unused script. Unsuccessfully tried to combine forks and false positives.

# Creating a series of data of in range of 1-50.
# x = np.linspace(1, 15, 200)

x = [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,3,3,3,3,4,4,4,4,4,4,4,4,4,4,6,6,6,6,6,8,8,8,8,10,12]
# plt.bar(np.arange(len(s)), s)
mu, std = stats.norm.fit(x)

# Plot the PDF.
x = np.linspace(0, 12, 100)
p = stats.norm.pdf(x, mu, std)


fig, ax1 = plt.subplots()

ax1.plot(x, p, 'k', linewidth=2)
ax1.set_xlabel("Number of false positives")
ax1.set_ylabel("Relative frequency")

# Forks data
stars = [1000,41,46,0,134,134,134,134,134,2,1,49,178,0,86,1000,75,76,0,138,102,110,40,0,8,0,0,48,1000,2,1,0,1,82,1000,1000,1000,1000,55,1,0,0,6,26,1,34,137,1,0,5,9,1,0,1,0,1,3,3,0,0,0,0,9,0,0,5,0,1,0,1,2,40,0,0,58,5,4,22,305,485,0,0,31,37,13,0,1,0,177,267,1,1,4,0,15,1,0,0,104,35,22,0,0,113,85,1,0,42,55,82,3,0,0,5,1000,46,0,4,12,37,10,17,78,7,1,26,3,9,20,12,0,544,1,0,502,502,0,16,102,11,7,6,14,0,5,8,48,70,0,2,0,5,40,1,57,12,380,0,8,1000,4,0,1,5,116,635,14,7,18,0,17,64,0,4,7,3,0,2,1000,0,1]
s = np.array([float(x/1000 * 0.4) for x in stars])
x_s = np.arange(0,max(x),max(x)/len(s))


# Plot bars
ax2 = ax1.twinx()
ax2.bar( x_s, s, 0.08)
yticklocs = ax2.yaxis.get_ticklocs()
yticklocs = np.arange(0,1000, 100)
# ticklocs = np.append(ticklocs, [900,1000])
ax2.yaxis.set_ticklabels( map( str, yticklocs ) )
ax2.set_ylabel("Stars")
xticklocs = ax2.xaxis.get_ticklocs()
xticklocs = np.arange(0,len(stars), int(len(stars)/7))
ax3 = ax1.twiny()
ax3.set_xlim(0,182)
ax3.set_xlabel("Projects")

plt.show()
