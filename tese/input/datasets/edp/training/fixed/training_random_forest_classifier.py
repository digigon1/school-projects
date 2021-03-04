# import autokeras
import math

import pandas
import numpy as np

from sklearn.ensemble import RandomForestClassifier
from sklearn.tree import export_graphviz

export_trees_images = False

files = ['final_input_T1.csv', 'final_input_T6.csv', 'final_input_T7.csv', 'final_input_T9.csv', 'final_input_T11.csv']

extracted_list = 3
test_file = files[extracted_list]
train_files = files[:extracted_list] + files[extracted_list + 1:]

rf = RandomForestClassifier(n_estimators = 1000, verbose = 2, class_weight='balanced',n_jobs=-1)
X = []
Y = []
for file in train_files:
	print('=============' + file + '=============')
	
	df = pandas.read_csv(file)

	df['Component_NONE'] = 0.0
	df.loc[((df['Component_GENERATOR'] == 0) & (df['Component_HYDRAULIC_GROUP'] == 0) & (df['Component_GENERATOR_BEARING'] == 0) & (df['Component_TRANSFORMER'] == 0) & (df['Component_GEARBOX'] == 0)), 'Component_NONE'] = 1.0
	
	Y_df = df[['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX', 'Component_NONE']]
	X_df = df.drop(columns=['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX', 'Component_NONE'])

	# print(Y_df)

	X.extend(X_df.values.tolist())
	Y.extend(Y_df.values.tolist())

	

	
empties = []
for i in range(len(X)):
	for f in X[i]:
		try:
			if math.isnan(f) or math.isinf(f):
				empties.append(i)
				break
		except Exception:
			print(f)
			
for index in sorted(empties, reverse = True):
	del X[index]
	del Y[index]
	
def get_class(classes):
	max_i = 0
	for i in range(len(classes)):
		if classes[max_i] < classes[i]:
			max_i = i
	return max_i
	
Y = [get_class(f) for f in Y]
# Fit to all data
rf.fit(X, Y)
	
	
df = pandas.read_csv(test_file)

df['Component_NONE'] = 0.0
df.loc[((df['Component_GENERATOR'] == 0) & (df['Component_HYDRAULIC_GROUP'] == 0) & (df['Component_GENERATOR_BEARING'] == 0) & (df['Component_TRANSFORMER'] == 0) & (df['Component_GEARBOX'] == 0)), 'Component_NONE'] = 1.0	

Y_df = df[['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX', 'Component_NONE']]
X_df = df.drop(columns=['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX', 'Component_NONE'])
X_test = X_df.values.tolist()
Y_test = Y_df.values.tolist()

empties = []
for i in range(len(X_test)):
	for f in X_test[i]:
		try:
			if math.isnan(f) or math.isinf(f):
				empties.append(i)
				break
		except Exception:
			print(f)
			
for index in sorted(empties, reverse = True):
	del X_test[index]
	del Y_test[index]

Y_test = [get_class(f) for f in Y_test]

print(rf.score(X_test, Y_test))

for i in range(len(rf.estimators_)):
	est = rf.estimators_[i]
	if export_trees_images: # EXTREMELY SPACE INTENSIVE
		export_graphviz(est, out_file='images/trees/tree_'+str(i)+'.dot', class_names=['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX', 'Component_NONE'])

def best_prediction(features):
	global model
	result = model.predict(np.array(features))
	vals = []
	for feat in range(len(features)):
		max_index = 0
		for i in range(len(result[feat])):
			if result[feat][i] > result[feat][max_index]:
				max_index = i
		vals.append(['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX', 'Component_NONE'][max_index])
	
	return vals


print('Enter commands now (exit and help exist):')
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


