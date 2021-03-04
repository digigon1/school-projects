# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""

from numpy import loadtxt
import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import Ridge

def random_split(data, test_points):
    ranks = np.arange(data.shape[0])
    np.random.shuffle(ranks)
    train = data[ranks>=test_points,:]
    test = data[ranks<test_points,:]
    return train,test

data = loadtxt("bluegills.txt")

scale = np.max(data, axis = 0)
data = data/scale

train, temp = random_split(data, len(data)/2)
valid, test = random_split(temp, len(temp)/2)

def mean_square_error(data,coefs):
    """Return mean squared error
    X on first column, Y on second column
    """
    pred = np.polyval(coefs,data[:,0])
    error = np.mean((data[:,1]-pred)**2)
    return error

best_err = 10000000 # very large number
for degree in range(1,6):
    coefs = np.polyfit(train[:,0],train[:,1],degree)
    valid_error = mean_square_error(valid,coefs)
    if valid_error < best_err:
        best_err = valid_error
        best_coef = coefs
        best_degree = degree

test_error = mean_square_error(test,best_coef)

plt.plot(train[:,0], train[:,1], 'ro')
plt.plot(valid[:,0], valid[:,1], 'bo')
plt.plot(test[:,0], test[:,1], 'go')

f = np.poly1d(best_coef)

xp = np.linspace(min(data[:,0]), max(data[:,0]), 100)

plt.plot(xp, f(xp))

plt.show()

print(best_degree,test_error)

def expand(data,degree):
    """expands the data to a polynomial of specified degree"""
    expanded = np.zeros((data.shape[0],degree+1))
    expanded[:,0]=data[:,0]
    expanded[:,-1]=data[:,-1]
    for power in range(2,degree+1):
        expanded[:,power-1]=data[:,0]**power
    return expanded

data = expand(data, 10)
train, temp = random_split(data, len(data)/2)
valid, test = random_split(temp, len(temp)/2)

lambs = np.linspace(0.01,0.2)
bestYs = []
result = []
best_err = 10000000 # very large number
for lamb in lambs:
    solver = Ridge(alpha = lamb, solver='cholesky',tol=0.00001)
    solver.fit(train[:,:-1],train[:,-1])
    ys = solver.predict(valid[:,:-1])
    valid_err = np.mean((ys-valid[:,-1])**2)
    result.append([lamb, valid_err])
    if valid_err<best_err:
        # keep the best
        bestYs = ys

result = np.array(result)

plt.plot(result[:,0], result[:,1], 'o')
plt.show()
    
    
'''
means = np.mean(Xs, 0)
stdevs = np.std(Xs, 0)

Xs = (Xs - means) / stdevs

print(Xs)
'''