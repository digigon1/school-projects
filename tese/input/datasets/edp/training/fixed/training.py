# import autokeras


import pandas
import numpy as np

from keras.models import Sequential
from keras.layers import LSTM, Dense, Dropout, Masking, Embedding


df = pandas.read_csv('final_input_T1.csv')

Y_df = df[['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX', 'Component_NONE']]
X_df = df.drop(columns=['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX', 'Component_NONE'])

X = X_df.values.tolist()
Y = Y_df.values.tolist()

split_point = int(len(X) * 0.005)

X_train = X[:split_point]
X_test = X[split_point:]

Y_train = Y[:split_point]
Y_train = np.array(Y_train)
Y_test = Y[split_point:]

feature_count = len(X[0])
outputs = len(Y[0])

time_window = 6 * 24 # one day of training (10 minutes * 6 * 24, 144)


train_features = [X_train[i - time_window : i + 1] for i in range(time_window, len(X_train))]
train_features = np.array(train_features)

test_features = [X_test[i - time_window : i + 1] for i in range(time_window, len(X_test))]
# all_test_features = np.array(test_features)


model = Sequential()

model.add(LSTM(units=512, input_shape=(time_window + 1, feature_count), return_sequences=True))
model.add(Dropout(0.1))
model.add(LSTM(units=outputs))
model.add(Dense(outputs))
model.compile(loss='mean_squared_error', optimizer='adam')


model.fit(train_features, Y_train[time_window:], epochs=1, batch_size=1, verbose=1)

# print(test_features)


score=model.evaluate(np.array(test_features[:time_window * 2]), np.array(Y_test[time_window:][:time_window * 2]), verbose=1)
print(score)

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


