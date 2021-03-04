# import autokeras

import sys
import os
import datetime

import pandas
import numpy as np

from sklearn.preprocessing import MinMaxScaler

from keras.models import Sequential
from keras.layers import CuDNNLSTM, CuDNNGRU, Dense, Dropout, Masking, Embedding, Activation
from keras.layers.normalization import BatchNormalization
from keras.optimizers import RMSprop, SGD
from keras.utils import Sequence
from keras.callbacks import ModelCheckpoint

test_folder = 'models/2020-02-01T19:58:56.173105/'
# test_folder = None

prev_model = 'models/2020-02-01T19:58:56.173105/weights.140-0.42129.hdf5'
# prev_model = None

batch_size = 100
epochs = 200
done_epochs = 140

class CustomSequence(Sequence):
	def __init__(self, x, y, window_size, batch_size):
		self.x = x
		self.y = y
		self.window_size = window_size
		self.batch_size = batch_size

		self.sizes = [int(np.ceil((len(a) - self.window_size) / self.batch_size)) for a in self.x]
		
	def __len__(self):
		return sum(self.sizes)
		
	def __getitem__(self, idx):
		# print()
		# print('   start idx: ' + str(idx))

		dataset = 0
		for size in self.sizes:
			if idx < size:
				break
			idx -= size
			dataset += 1

		# pick correct dataset
		# dataset = int(idx // (int(np.ceil((len(self.x[0]) - self.window_size) / self.batch_size))))

		# calculate index in file
		# idx = int(idx % (int(np.ceil((len(self.x[0]) - self.window_size) / self.batch_size))))

		# return batch
		x = np.array(self.x[dataset])
		y = np.array(self.y[dataset])

		range_start = idx * self.batch_size + self.window_size
		range_end = min((idx + 1) * self.batch_size + self.window_size, len(x))
		# print('   start: ' + str(range_start) + ", end: " + str(range_end))
		X_batch = np.array([x[i - self.window_size : i + 1] for i in range(range_start, range_end)])
		Y_batch = np.array(y[range_start : range_end])
		weights = np.zeros(len(Y_batch))
		weights[:] = 1.
		weights[np.unique(np.where(Y_batch > 0)[0])] = 10.
		# print('   return values: ' + str(len(X_batch)) + ", " + str(len(Y_batch)) + ", " + str(len(weights)))
		return X_batch, Y_batch, weights

X_array = []
Y_array = []

files = ['final_input_T1.csv', 'final_input_T6.csv', 'final_input_T7.csv', 'final_input_T9.csv', 'final_input_T11.csv']

for file in files:
	df = pandas.read_csv(file)

	Y_df = df[['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX']]
	X_df = df.drop(columns=['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX'])

	X = X_df.values
	X = MinMaxScaler().fit_transform(X)
	# print(pandas.DataFrame(X))
	X = X.tolist()

	Y = Y_df.values
	# Y = MinMaxScaler().fit_transform(Y)
	# print(pandas.DataFrame(Y))
	Y = Y.tolist()

	X_array.append(X)
	Y_array.append(Y)

# Y = [[5 - a.index(max(a))] for a in Y]
# print(Y)
# print(list(filter(lambda p: p[1][0] != 0, [(i, Y[i]) for i in range(len(Y))])))

# Y = [[1 if b == 0 else 2 for b in a] for a in Y]

split_point = int(len(X_array[0]) * 0.8)

X_train = X_array[0][:split_point]
X_test = X_array[0][split_point:]

Y_train = Y_array[0][:split_point]
Y_train = np.array(Y_train)
Y_test = Y_array[0][split_point:]


feature_count = len(X_array[0][0])
outputs = len(Y_array[0][0])

checks_in_day = 6 * 24 # one day of training (10 minutes * 6 * 24, 144)

# time_window = checks_in_day
time_window = checks_in_day * 7

seq = CustomSequence(X_array, Y_array, time_window, batch_size=batch_size)

# train_features = np.array([X_train[i - time_window : i + 1] for i in range(time_window, len(X_train))])


test_features = [X_test[i - time_window : i + 1] for i in range(time_window, len(X_test))]

# test for NaN
'''
all_test_features = np.array(test_features)
result = np.where(np.isnan(all_test_features))
coords = list(zip(result[0], result[1], result[2]))
for coord in coords:
	print(coord)
sys.exit(1)
'''



model = Sequential()

# model.add(BatchNormalization())

# model.add(Dense(outputs, input_shape=(time_window + 1, feature_count)))

# model.add(LSTM(units=256, input_shape=(time_window + 1, feature_count), return_sequences=True))
model.add(BatchNormalization(input_shape=(time_window + 1, feature_count)))
model.add(Dropout(0.2))
model.add(CuDNNLSTM(units=256, input_shape=(time_window + 1, feature_count), return_sequences=False))
model.add(Dropout(0.5))
model.add(Dense(outputs))
# model.add(CuDNNLSTM(units=outputs, return_sequences=False))
# model.add(CuDNNGRU(units=outputs, input_shape=(time_window + 1, feature_count), return_sequences=False))
# model.add(LSTM(units=outputs))

# model.add(Dense(outputs))
# model.add(Activation('softmax'))
# opt = RMSprop(0.001)
opt = SGD()
model.compile(loss='mean_squared_error', optimizer=opt, metrics=['accuracy'])

date = str(datetime.datetime.now().isoformat())

class TestCheckPoint(ModelCheckpoint):
	"""docstring for TestCheckPoint"""
	def __init__(self, name, date):
		super(TestCheckPoint, self).__init__(name, monitor='acc')
		self.date = date

	def on_epoch_end(self, epoch, logs=None):
		if prev_model is None:
			try:
				os.mkdir('models/' + self.date)
			except Exception as e:
				pass
		super().on_epoch_end(epoch, logs)
		

if test_folder is not None:
	files = os.listdir(test_folder)
	files.sort()
	for file in files:
		if file.find('weights') != -1:
			number = file.split('.')[1].split('-')[0]
			model.load_weights(test_folder + '/' + file)
			score = model.evaluate(np.array(test_features), np.array(Y_test[time_window:]), verbose=1)
			print(number + ' SCORE: ' + str(model.metrics_names) + " " + str(score))

else:

	if prev_model is not None:
		print('===== Loading previous weights =====')
		model.load_weights(prev_model)
		folder = '/'.join(prev_model.split('/')[:-1])
		print('===== Done loading =====')
	else:
		folder = 'models/' + date

	checkpoint_callback = TestCheckPoint(folder + '/weights.{epoch:03d}-{acc:.5f}.hdf5', date)
	# checkpoint_callback = ModelCheckpoint('models/' + date + '/weights.{epoch:03d}-{acc:.5f}.hdf5', monitor='acc')

	# model.fit(train_features, Y_train[time_window:], epochs=1000, batch_size=100, verbose=1)
	print(str(done_epochs) + '/'+ str(epochs))
	print('===== Fitting =====')
	model.fit_generator(seq, initial_epoch=done_epochs, epochs=epochs, use_multiprocessing=False, verbose=1, callbacks=[checkpoint_callback])

	print('===== Evaluating =====')
	score = model.evaluate(np.array(test_features), np.array(Y_test[time_window:]), verbose=1)
	print('SCORE: ' + str(model.metrics_names) + " " + str(score))



def best_prediction(features):
	global model
	result = model.predict(np.array(features))
	vals = []
	for feat in range(len(features)):
		max_index = -1
		max_val = 0.0499999
		for i in range(0, len(result[feat])):
			if result[feat][i] > max_val:
				max_index = i
				max_val = result[feat][i]
		if max_index == -1:
			vals.append('Component_NONE')
		else:
			vals.append(['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX'][max_index])
	
	return vals

# [model.predict(np.array([[[a] * 106] * 145])) for a in np.arange(0, 1.1, 0.1)]

print('ready')
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


