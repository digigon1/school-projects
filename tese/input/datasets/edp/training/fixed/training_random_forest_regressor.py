# import autokeras
import math

import pandas
import numpy as np

from sklearn.ensemble import RandomForestRegressor


df = pandas.read_csv('final_input_T11.csv')

Y_df = df[['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX']]
X_df = df.drop(columns=['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX'])

X = X_df.values.tolist()
Y = Y_df.values.tolist()

split_point = int(len(X) * 0.8)

X_train = X[:split_point]
X_test = X[split_point:]

Y_train = Y[:split_point]
# Y_train = np.array(Y_train)
Y_test = Y[split_point:]

feature_count = len(X[0])
outputs = len(Y[0])

checks_in_day = 6 * 24 # one day of training (10 minutes * 6 * 24, 144)

time_window = checks_in_day


# train_features = [X_train[i - time_window : i + 1] for i in range(time_window, len(X_train))]
# train_features = np.array(train_features)

# test_features = [X_test[i - time_window : i + 1] for i in range(time_window, len(X_test))]
# all_test_features = np.array(test_features)
empties = []
for i in range(len(X_train)):
	for f in X_train[i]:
		if math.isnan(f) or math.isinf(f):
			empties.append(i)
			break

for index in sorted(empties, reverse = True):
	del X_train[index]
	del Y_train[index]


empties = []
for i in range(len(X_test)):
	for f in X_test[i]:
		if math.isnan(f) or math.isinf(f):
			empties.append(i)
			break

for index in sorted(empties, reverse = True):
	del X_test[index]
	del Y_test[index]

rf = RandomForestRegressor(n_estimators = 1000, verbose = 2, n_jobs=-1)
rf.fit(X_train, Y_train)

# 0.0
print(rf.score(X_test, Y_test))


def best_prediction(features):
	global model
	result = model.predict(np.array(features))
	vals = []
	for feat in range(len(features)):
		max_index = 0
		for i in range(len(result[feat])):
			if result[feat][i] > result[feat][max_index]:
				max_index = i
		vals.append(['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX'][max_index])
	
	return vals


while True:
	code = input()
	if code == 'exit':
		break
	if code == 'help':
		print('best_prediction([test_features[...]])')
		continue
	try:
		print(eval(code))
	except Exception as e:
		print('Error')
		print(e)


