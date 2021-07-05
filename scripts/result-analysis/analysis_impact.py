import pandas as pd
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import scipy.stats as stats
import matplotlib.mlab as mlab
import math

# Creating a series of data of in range of 1-50.
# x = np.linspace(1, 15, 200)

x = [2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 8, 10, 10, 12, 12, 12, 14, 16, 16, 16, 16, 18, 22, 24, 24, 24, 24, 26, 32, 36, 40, 44, 44, 44, 44, 44, 44, 52, 52, 64, 64, 74, 76, 84, 88, 88, 88, 88, 88, 94, 100, 110, 132, 132, 132, 162, 188, 212, 228, 248, 264, 290, 356, 422, 610, 612, 664, 858, 912, 1000, 1026, 1152, 1252, 1470, 1530, 1900, 2094, 2580, 2652, 2652, 2860, 3360]#, 19272, 27592]
# plt.bar(np.arange(len(s)), s)
# mu, std = stats.norm.fit(x)
mu = np.median(x)
std = np.std(x)
# h_n = (x - mu) / std

print(mu)
print(std)



# Plot the histogram.
plt.hist(x, bins=90,  alpha=0.6, color='b')

plt.xlabel("Internal method calls at risk")
plt.ylabel("Projects")

plt.show()
# plt.savefig('fp_graph.png', dpi=600)
