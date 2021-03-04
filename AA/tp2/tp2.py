# -*- coding: utf-8 -*-
"""
Created on Thu Nov 30 16:49:16 2017

@author: Goncalo
"""

import csv
import numpy as np
import matplotlib.pyplot as plt
from scipy.misc import imread

from sklearn.cluster import DBSCAN
from sklearn.neighbors import NearestNeighbors
from sklearn.metrics import adjusted_rand_score, silhouette_score
from sklearn.mixture import GaussianMixture

from itertools import combinations

#TIMING
from contextlib import contextmanager
import time
@contextmanager
def timeit_context(name):
    startTime = time.time()
    yield
    elapsedTime = time.time() - startTime
    print('[{}] finished in {} ms'.format(name, int(elapsedTime * 1000)))
    
RADIUS = 6371

MANUAL = True
MAX_OFFSET = 40 #40
RAND_INDEX = 0 #0 for rand, 4 for adjusted
plot = 0
labels = ['rand', 'f1', 'precision', 'recall', 'adjusted rand', 'silhouette']

class Point():
    def __init__(self, lat, lon, fault):
        self.lat = lat
        self.lon = lon
        self.x = RADIUS * np.cos(lat * np.pi/180) * np.cos(lon * np.pi/180)
        self.y = RADIUS * np.cos(lat * np.pi/180) * np.sin(lon * np.pi/180)
        self.z = RADIUS * np.sin(lat * np.pi/180)
        self.fault = int(fault)
        
    def __repr__(self):
        return str(self.x) + ' ' + str(self.y) + ' ' + str(self.z) + ' ' + str(self.fault)

def plot_classes(labels,lon,lat, alpha=0.5, edge = 'k'):
    """Plot seismic events using Mollweide projection.
    Arguments are the cluster labels and the longitude and latitude
    vectors of the events"""
    img = imread("Mollweide_projection_SW.jpg")        
    plt.figure(figsize=(10,5),frameon=False)    
    x = lon/180*np.pi
    y = lat/180*np.pi
    ax = plt.subplot(111, projection="mollweide")
    print(ax.get_xlim(), ax.get_ylim())
    t = ax.transData.transform(np.vstack((x,y)).T)
    print(np.min(np.vstack((x,y)).T,axis=0))
    print(np.min(t,axis=0))
    clims = np.array([(-np.pi,0),(np.pi,0),(0,-np.pi/2),(0,np.pi/2)])
    lims = ax.transData.transform(clims)
    plt.close()
    plt.figure(figsize=(10,5),frameon=False)    
    plt.subplot(111)
    plt.imshow(img,zorder=0,extent=[lims[0,0],lims[1,0],lims[2,1],lims[3,1]],aspect=1)        
    x = t[:,0]
    y= t[:,1]
    nots = np.zeros(len(labels)).astype(bool)
    diffs = np.unique(labels)    
    ix = 0   
    for lab in diffs:
        if int(lab) >= 0:
            mask = labels==lab
            nots = np.logical_or(nots,mask)
            plt.plot(x[mask], y[mask],'o', markersize=4, mew=1,zorder=1,alpha=alpha, markeredgecolor=edge)
            ix = ix+1                    
    mask = np.logical_not(nots)    
    if np.sum(mask)>0:
        plt.plot(x[mask], y[mask], '.', markersize=1, mew=1,markerfacecolor='w', markeredgecolor=edge)
    plt.grid('off')
    plt.axis('off')
    plt.show()
    plt.gcf().clear()
    

def plot_scores(scores, min_r=2, max_r=50):
    global plot, labels
    xs = range(min_r, max_r)
    legends = []
    for i in range(0, len(scores[0])):
        a, = plt.plot(xs, scores[:, i], label=labels[i])
        legends.append(a)
    legend = plt.legend(handles=legends, bbox_to_anchor=(1.05, 1))
    plt.grid('on')
    plt.savefig('plot_'+str(plot)+'.png', bbox_extra_artists=(legend,), bbox_inches='tight')
    plt.gcf().clear()
    plot += 1
 
def calc_scores(labels, points):
    n = len(labels)
    tp = 0
    fp = 0
    fn = 0
    tn = 0
    for i, j in combinations(range(0, len(labels)), 2):
        if points[i].fault == points[j].fault:
            if labels[i] == labels[j]:
                tp += 1
            else:
                fn += 1
        else:
            if labels[i] == labels[j]:
                fp += 1
            else:
                tn += 1
    precision = tp/(tp+fp)
    recall = tp/(tp+fn)
    rand = 2*(tp+tn)/(n**2 - n)
    f1 = 2*precision*recall/(precision+recall)
    adjusted_rand = adjusted_rand_score([p.fault for p in points], labels)
    silhouette = silhouette_score(point_to_coords(points), labels)
    result = (rand, f1, precision, recall, adjusted_rand, silhouette)
    #print(result)
    return result


points = []
with open('tp2_data.csv') as csvfile:
    reader = csv.DictReader(csvfile)
    for r in reader:
        points.append(Point(float(r['latitude']), float(r['longitude']), r['fault']))

point_to_coords = lambda l: np.array([[p.x, p.y, p.z] for p in l])
coords = point_to_coords(points)


print(silhouette_score(point_to_coords(points), [p.fault for p in points]))

'''
#K-Means
print('------------------------- K-MEANS -------------------------')
def closest_centroids(data,centroids):
    ys = np.zeros(data.shape[0])
    for ix in range(data.shape[0]):
        dists = np.sum((centroids-data[ix,:])**2,axis=1)
        ys[ix] = np.argmin(dists)
    return ys

def adjust_centroids(data,centroids):
    ys = closest_centroids(data,centroids)
    for ix in range(centroids.shape[0]):
        centroids[ix,:] = np.mean(data[ys==ix,:],axis=0)

def forgy(data,k):
    ixs = np.arange(data.shape[0])
    np.random.shuffle(ixs)
    return data[:k].copy()
            

max_rand = (0, 0, [])
kmeans_scores = []
for k in np.arange(2, 50):
    print('calculating k='+str(k))
    centroids = forgy(coords, k)
    prev = [0, 0, 0]
    delta = 0.001
    while np.sum((prev - centroids)**2) > delta:
        prev = centroids.copy()
        adjust_centroids(coords, centroids)
    
    k_means_labels = closest_centroids(coords, centroids)
    rand = calc_scores(k_means_labels, points)
    kmeans_scores.append(rand)
    print('rand for k='+str(k)+': '+str(rand[RAND_INDEX]))
    if rand[RAND_INDEX] > max_rand[0]:
        max_rand = (rand[RAND_INDEX], k, k_means_labels)
        
kmeans_scores = np.array(kmeans_scores)
plot_scores(kmeans_scores)

print('max value found for k='+str(max_rand[1]))
print('max value is '+str(max_rand[0]))
plot_classes(max_rand[2], np.array([p.lon for p in points]), np.array([p.lat for p in points]))
'''


#DBSCAN
print('------------------------- DBSCAN -------------------------')
num_noise = 0
for p in points:
    if p.fault == -1:
        num_noise += 1

nbrs = NearestNeighbors(n_neighbors=4, algorithm='ball_tree').fit(coords)
distances, indices = nbrs.kneighbors(coords)
distances = distances[:,3]
distances = np.sort(distances, axis=None)[::-1]
answer = ''
min_num = 0
max_num = len(distances) - 1

if MANUAL:
    while True:
        plt.plot(range(0, len(distances)), distances)
        for i in np.arange(0, distances[num_noise], distances[num_noise]/100):
            plt.plot(num_noise, i, 'rs')
        plt.show()
        answer = input('Is this eps ok? (yes or no)')
        if answer == 'yes':
            break
        elif answer == 'no':
            while True:
                answer = input('Input higher or lower? (+, - or value to add to num_noise)')
                if answer == '+':
                    min_num = num_noise
                    num_noise = int((max_num + num_noise)/2)
                    break
                elif answer == '-':
                    max_num = num_noise
                    num_noise = int((min_num + num_noise)/2)
                    break
                else:
                    try:
                        num_noise += int(answer)
                        break
                    except:
                        print('Invalid value')
        else:
            print('invalid answer')
else:
    dbscan_auto_scores = []
    
    max_offset = MAX_OFFSET
    initial_scores = calc_scores(DBSCAN(eps = distances[num_noise], min_samples = 4, n_jobs = -1).fit_predict(coords), points)
    initial_found = initial_scores[RAND_INDEX]
    lower_found = (initial_found, num_noise)
    
    dbscan_auto_scores.append((num_noise, initial_scores))
    
    lower_attempt = num_noise - 1
    print('initial attempt: '+str(num_noise))
    while max_offset > 0:
        print('lower_found: '+str(lower_found))
        print('trying '+str(lower_attempt))
        scan = DBSCAN(eps = distances[lower_attempt], min_samples = 4, n_jobs = -1)
        dbscan_labels = scan.fit_predict(coords)
        scores = calc_scores(dbscan_labels, points)
        dbscan_auto_scores.append((lower_attempt, scores))
        if scores[RAND_INDEX] >= lower_found[0]:
            max_offset = MAX_OFFSET
            lower_found = (scores[RAND_INDEX], lower_attempt)
        else:
            max_offset -= 1
        lower_attempt -= 1
    
    higher_attempt = num_noise + 1
    max_offset = MAX_OFFSET
    higher_found = (initial_found, num_noise)
    while max_offset > 0:
        print('higher_found: '+str(higher_found))
        print('trying '+str(higher_attempt))
        scan = DBSCAN(eps = distances[higher_attempt], min_samples = 4, n_jobs = -1)
        dbscan_labels = scan.fit_predict(coords)
        scores = calc_scores(dbscan_labels, points)
        dbscan_auto_scores.append((higher_attempt, scores))
        if scores[RAND_INDEX] >= higher_found[0]:
            max_offset = MAX_OFFSET
            higher_found = (scores[RAND_INDEX], higher_attempt)
        else:
            max_offset -= 1
        higher_attempt += 1
        
    if initial_found < lower_found[0]:
        initial_found = lower_found[0]
        num_noise = lower_found[1]
        
    if initial_found < higher_found[0]:
        num_noise = higher_found[1]
    
    dbscan_auto_scores = sorted(dbscan_auto_scores, key=lambda t: t[0])
    min_r = dbscan_auto_scores[0][0]
    max_r = dbscan_auto_scores[-1][0] + 1
    dbscan_auto_scores = np.array([t[1] for t in dbscan_auto_scores])
    print(dbscan_auto_scores)
    plot_scores(dbscan_auto_scores, min_r, max_r)
    #print(dbscan_auto_scores)

    
print('num_noise: '+str(num_noise))
print('eps = '+str(distances[num_noise]))

scan = DBSCAN(eps = distances[num_noise], min_samples = 4, n_jobs = -1)
dbscan_labels = scan.fit_predict(coords)
plot_classes(dbscan_labels, np.array([p.lon for p in points]), np.array([p.lat for p in points]))
print(calc_scores(dbscan_labels, points))



#Gaussian Mixture Models
print('------------------------- GMM -------------------------')
gmm_scores = []
max_gmm_rand = (0, 0, [])
for i in range(2, 50):
    print('calculating i='+str(i))
    gmm = GaussianMixture(n_components = i)
    gmm.fit(coords)
    gmm_labels = gmm.predict(coords)
    rand = calc_scores(gmm_labels, points)
    gmm_scores.append(rand)
    print('rand for i='+str(i)+': '+str(rand[RAND_INDEX]))
    if rand[RAND_INDEX] > max_gmm_rand[0]:
        max_gmm_rand = (rand[RAND_INDEX], i, gmm_labels)

gmm_scores = np.array(gmm_scores)

plot_scores(gmm_scores)   

        
plot_classes(max_gmm_rand[2], np.array([p.lon for p in points]), np.array([p.lat for p in points]))
print('max rand for '+str(max_gmm_rand[1])+' components')
print('max rand: '+str(max_gmm_rand[0]))
