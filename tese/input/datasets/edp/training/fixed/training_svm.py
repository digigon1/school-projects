# import autokeras
import math

import pandas
import numpy as np

from sklearn.svm import SVC

files = ['final_input_T1.csv', 'final_input_T6.csv', 'final_input_T7.csv', 'final_input_T9.csv', 'final_input_T11.csv']

extracted_list = 3
test_file = files[extracted_list]
train_files = files[:extracted_list] + files[extracted_list + 1:]

# Auxiliary functions
def get_class(classes):
	max_i = 0
	for i in range(len(classes)):
		if classes[max_i] < classes[i]:
			max_i = i
	return max_i

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

# Loading training values
X = []
Y = []
for file in train_files:
	print('=============' + file + '=============')
	
	df = pandas.read_csv(file)
	Y_df = df[['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX']]
	X_df = df.drop(columns=['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX'])

	# drop 99%+ correlated columns
	X_df = X_df.drop(columns=['Grd_Prod_PsbleCap_Avg', 'Rtr_RPM_Min', 'Grd_Prod_PsblePwr_Avg', 'Gen_Phase3_Temp_Avg', 'Amb_WindSpeed_Est_Avg', 'Gen_Phase2_Temp_Avg', 'Rtr_RPM_Avg', 'Grd_Prod_CurPhse2_Avg', 'Rtr_RPM_Max', 'Gen_Phase3_Temp_Avg', 'Grd_Prod_CurPhse3_Avg', 'Grd_Prod_ReactPwr_Avg', 'Grd_Prod_Pwr_Avg'])

	# drop 98%+ correlated columns
	X_df = X_df.drop(columns=['Prod_LatestAvg_TotReactPwr', 'Prod_LatestAvg_ActPwrGen1', 'Prod_LatestAvg_TotActPwr', 'Grd_Prod_CurPhse1_Avg', 'Grd_Prod_PsbleCap_Min', 'Grd_Prod_PsblePwr_Max'])

	X.extend(X_df.values.tolist())
	Y.extend(Y_df.values.tolist())
		

print('Cleaning data')
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
	
Y = [get_class(f) for f in Y]


# Loading testing values
df = pandas.read_csv(test_file)
Y_df = df[['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX']]

X_df = df.drop(columns=['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX'])
# drop 99%+ correlated columns
X_df = X_df.drop(columns=['Grd_Prod_PsbleCap_Avg', 'Rtr_RPM_Min', 'Grd_Prod_PsblePwr_Avg', 'Gen_Phase3_Temp_Avg', 'Amb_WindSpeed_Est_Avg', 'Gen_Phase2_Temp_Avg', 'Rtr_RPM_Avg', 'Grd_Prod_CurPhse2_Avg', 'Rtr_RPM_Max', 'Gen_Phase3_Temp_Avg', 'Grd_Prod_CurPhse3_Avg', 'Grd_Prod_ReactPwr_Avg', 'Grd_Prod_Pwr_Avg'])
# drop 98%+ correlated columns
X_df = X_df.drop(columns=['Prod_LatestAvg_TotReactPwr', 'Prod_LatestAvg_ActPwrGen1', 'Prod_LatestAvg_TotActPwr', 'Grd_Prod_CurPhse1_Avg', 'Grd_Prod_PsbleCap_Min', 'Grd_Prod_PsblePwr_Max'])

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


# Testing C_exp values
max_val = (-1, -4)

scores = []

for C_exp in range(20, -21, -1):
	print('C_exp = ' + str(C_exp))
	svm = SVC(kernel='linear', C=2**C_exp, verbose=False)
		
	print('Fitting')
	# Fit to all data
	svm.fit(X, Y)

	print('Testing')
	score = svm.score(X_test, Y_test)
	scores.append(score)
	print('score = ' + str(score))
	if score > max_val[0]:
		max_val = (score, C_exp)

print('max_val = ' + str(max_val))

print()
print('==== SCORES ====')
print(scores)

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


