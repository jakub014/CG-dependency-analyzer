import pandas as pd
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import scipy.stats as stats
import matplotlib.mlab as mlab
import math

# Creating a series of data of in range of 1-50.
# x = np.linspace(1, 15, 200)

x = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 9, 10, 10, 10, 14, 14, 17, 39, 90]#, 418]
# plt.bar(np.arange(len(s)), s)
# mu, std = stats.norm.fit(x)
mu = np.mean(x)
std = np.std(x)
# h_n = (x - mu) / std

print(mu)
print(std)



# Plot the histogram.
plt.hist(x, bins=90,  alpha=0.6, color='b')

plt.xlabel("External vulnerable methods")
plt.ylabel("Projects")

plt.show()
# plt.savefig('fp_graph.png', dpi=600)
