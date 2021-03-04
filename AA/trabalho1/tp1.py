# -*- coding: utf-8 -*-
"""
Created on Fri Oct 13 15:45:39 2017

@author: Goncalo
"""

from numpy import loadtxt
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from sklearn.linear_model import LogisticRegression
from sklearn.cross_validation import train_test_split, StratifiedKFold
from sklearn.neighbors import KernelDensity
from scipy.stats import mode


data = loadtxt("TP1-data.csv", delimiter=',')

#load data
Ys = data[:,4]
Xs = data[:,:4]

#standardizing data
means = np.mean(Xs, 0)
stdevs = np.std(Xs, 0)
Xs = (Xs-means)/stdevs

#splitting data (0.33 for tests, 0.67 for training)
X_r, X_t, Y_r, Y_t = train_test_split(Xs, Ys, test_size=0.33, stratify=Ys)

#calculating folds for training data
folds = 5
kf = StratifiedKFold(Y_r, n_folds=folds)



#logistic regression
def calc_fold(i, X_r, X_t, Y_r, Y_t):
    reg = LogisticRegression(C = 2**i, tol = 1e-10)
    reg.fit(X_r, Y_r)
    return reg.score(X_t, Y_t), reg.score(X_r, Y_r)

results = []
min_err_i = 0
min_va_err = np.Inf
for i in range(0, 20):
    tr_err = va_err = 0
    for tr_ix, va_ix in kf:
        v, r = calc_fold(i, X_r[tr_ix], X_r[va_ix], Y_r[tr_ix], Y_r[va_ix])
        tr_err += r
        va_err += v
    if 1-(va_err/folds) < min_va_err:
        min_va_err = 1-(va_err/folds)
        min_err_i = i
    results.append([2**i, tr_err/folds, va_err/folds])

#results
results = np.array(results)
test_line = plt.plot(np.log(results[:,0]), 1-results[:,1], 'r-')
train_line = plt.plot(np.log(results[:,0]), 1-results[:,2], 'b-')
test_legend = mpatches.Patch(color='red', label='Training Data')
train_legend = mpatches.Patch(color='blue', label='Test Data')
plt.legend(handles=[test_legend, train_legend])
plt.title('Logistic Regression')
plt.ylabel('Error')
plt.xlabel('Log of C')
plt.show()

reg = LogisticRegression(C = 2**min_err_i, tol = 1e-10)
reg.fit(X_r, Y_r)
logistic_accuracy = reg.predict(X_t) == Y_t

test_err, tr_err = calc_fold(min_err_i, X_r, X_t, Y_r, Y_t)
logistic_err = 1-test_err
print('C: '+str(2**min_err_i)+', Train: '+str(1-tr_err)+', Test: '+str(1-test_err))



#k-nearest neighbours
def mink_dist(x, X, p = 2):
    """return p-norm values of point x distance to vector X"""
    sq_diff = np.power(np.abs(X - x),p)
    dists = np.power(np.sum(sq_diff,1),1.0/p)
    return dists

def k_nearest_ixs(x, X,k):
    ixs = np.argsort(mink_dist(x,X))
    return ixs[:k]

def knn_classify(x,X,Y,k):
    ix = k_nearest_ixs(x,X,k)
    return mode(Y[ix], axis=None)[0][0]

def get_knn_results(k, X_t, X_r, Y_t, Y_r):
    temp_t = [knn_classify(x, X_r, Y_r, k) for x in X_t]
    unique_t, counts_t = np.unique(temp_t == Y_t, return_counts=True)
    temp_r = [knn_classify(x, X_r, Y_r, k) for x in X_r]
    unique_r, counts_r = np.unique(temp_r == Y_r, return_counts=True)
    return dict(zip(unique_t, counts_t))[True]/len(Y_t), dict(zip(unique_r, counts_r))[True]/len(Y_r)

results = []
min_err_k = 0
min_va_err = np.Inf
for k in np.arange(1, 40, 2):
    tr_err = va_err = 0
    for tr_ix, va_ix in kf:
        v, r = get_knn_results(k, X_r[tr_ix], X_r[va_ix], Y_r[tr_ix], Y_r[va_ix])
        tr_err += r
        va_err += v
    if 1-(va_err/folds) < min_va_err:
        min_va_err = 1-(va_err/folds)
        min_err_k = k
    results.append([k, tr_err/folds, va_err/folds])

#results
results = np.array(results)
test_line = plt.plot(results[:,0], 1-results[:,1], 'r-')
train_line = plt.plot(results[:,0], 1-results[:,2], 'b-')
test_legend = mpatches.Patch(color='red', label='Training Data')
train_legend = mpatches.Patch(color='blue', label='Test Data')
plt.legend(handles=[test_legend, train_legend])
plt.title('K-Nearest Neighbours')
plt.ylabel('Error')
plt.xlabel('K')
plt.show()

knn_accuracy = [knn_classify(x, X_r, Y_r, min_err_k) for x in X_t] == Y_t

test_err, tr_err = get_knn_results(min_err_k, X_r, X_t, Y_r, Y_t)
knn_err = 1-test_err
print('K: '+str(min_err_k)+', Train: '+str(1-tr_err)+', Test: '+str(1-test_err))



#naive bayes
def split(X, Y, cut):
    return X[Y < cut], X[Y >= cut]

def get_value(x, density):
    total = 0
    for j in range(0, 4):
        total += density[j].score(x[j])
    return total + density[4]

def classify(x, densities):
    results = [get_value(x, densities[0]), get_value(x, densities[1])]
    if results[0] > results[1]:
        return 0.
    else:
        return 1.

def get_bayes_results(bw, X_r, X_t, Y_r, Y_t):
    densities = [[], []]
    for i in range(0, X_r.shape[1]):
        x0, x1 = split(X_r, Y_r, 0.5)
        kde = KernelDensity(kernel='gaussian', bandwidth=bw)
        kde.fit(x0[:,i].reshape(-1, 1))
        densities[0].append(kde)
        kde = KernelDensity(kernel='gaussian', bandwidth=bw)
        kde.fit(x1[:,i].reshape(-1, 1))
        densities[1].append(kde)
    
    densities[0].append(np.log(len(x0) / len(X_r)))
    densities[1].append(np.log(len(x1) / len(X_r)))

    #classifying
    temp_t = [classify(x, densities) for x in X_t]
    unique_t, counts_t = np.unique(temp_t == Y_t, return_counts=True)
    temp_r = [classify(x, densities) for x in X_r]
    unique_r, counts_r = np.unique(temp_r == Y_r, return_counts=True)
    return dict(zip(unique_t, counts_t))[True]/len(Y_t), dict(zip(unique_r, counts_r))[True]/len(Y_r)

results = []
min_err_bw = 0
min_va_err = np.Inf
for bw in np.arange(0.01, 1, 0.02):
    #training
    tr_err = va_err = 0
    for tr_ix, va_ix in kf:
        v, r = get_bayes_results(bw, X_r[tr_ix], X_r[va_ix], Y_r[tr_ix], Y_r[va_ix])
        tr_err += r
        va_err += v
    if 1-(va_err/folds) < min_va_err:
        min_va_err = 1-(va_err/folds)
        min_err_bw = bw
    results.append([bw, tr_err/folds, va_err/folds])
    
#results
results = np.array(results)
test_line = plt.plot(results[:,0], 1-results[:,1], 'r-')
train_line = plt.plot(results[:,0], 1-results[:,2], 'b-')
test_legend = mpatches.Patch(color='red', label='Training Data')
train_legend = mpatches.Patch(color='blue', label='Test Data')
plt.legend(handles=[test_legend, train_legend])
plt.title('Naive Bayes')
plt.ylabel('Error')
plt.xlabel('Bandwidth')
plt.show()

densities = [[], []]
for i in range(0, X_r.shape[1]):
    x0, x1 = split(X_r, Y_r, 0.5)
    kde = KernelDensity(kernel='gaussian', bandwidth=min_err_bw)
    kde.fit(x0[:,i].reshape(-1, 1))
    densities[0].append(kde)
    kde = KernelDensity(kernel='gaussian', bandwidth=min_err_bw)
    kde.fit(x1[:,i].reshape(-1, 1))
    densities[1].append(kde)
densities[0].append(np.log(len(x0) / len(X_r)))
densities[1].append(np.log(len(x1) / len(X_r)))
bayes_accuracy = [classify(x, densities) for x in X_t] == Y_t

test_err, tr_err = get_bayes_results(min_err_bw, X_r, X_t, Y_r, Y_t)
bayes_err = 1-test_err
print('Bandwidth: '+str(min_err_bw)+', Train: '+str(1-tr_err)+', Test: '+str(1-test_err))
    

    
#McNemar's test
def count(l, thing):
    unique, counts = np.unique(l, return_counts=True)
    try:
        return dict(zip(unique, counts))[thing]
    except Exception:
        return 0

def mcnemar_test(count_wrong, count_right):
    e01 = count(count_wrong[count_right == True], False)
    e10 = count(count_wrong[count_right == False], True)
    return ((np.abs(e01 - e10) - 1) ** 2) / (e01 + e10) > 3.84

#Classifier comparation
if mcnemar_test(logistic_accuracy, knn_accuracy):
    if logistic_err < knn_err:
        print('Logistic is likely better than K-NN')
    else:
        print('K-NN is likely better than Logistic')
if mcnemar_test(knn_accuracy, bayes_accuracy):
    if knn_err < bayes_err:
        print('K-NN is likely better than Bayes')
    else:
        print('Bayes is likely better than K-NN')
if mcnemar_test(bayes_accuracy, logistic_accuracy):
    if bayes_err < logistic_err:
        print('Bayes is likely better than Logistic')
    else:
        print('Logistic is likely better than Bayes') 
