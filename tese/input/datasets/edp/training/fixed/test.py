import pandas
import matplotlib
matplotlib.use('Agg')

import matplotlib.pyplot as plt


values = pandas.read_csv('final_input_T1.csv')

failure_interval = (15112, 28634)

values['In_Failure'] = 0
values.loc[failure_interval[0]:failure_interval[1], 'In_Failure'] = 1

ignored_columns = ['In_Failure', 'Timestamp', 'Component_GEARBOX', 'Component_GENERATOR', 'Component_GENERATOR_BEARING', 'Component_HYDRAULIC_GROUP', 'Component_TRANSFORMER']

special_columns = {'Grd_Prod_Freq_Avg': 50, 'Grd_Prod_VoltPhse1_Avg': 400, 'Grd_Prod_VoltPhse2_Avg': 400, 'Grd_Prod_VoltPhse3_Avg': 400}

roll_windows = [1, 6, 12, 24, 48, 96]

for roll_window in roll_windows:
	for column in values.columns:
		if column not in ignored_columns:
			plt.clf()
			print(column)
			if column in special_columns:
				plt.gca().set_ylim(special_columns[column] * 0.96, special_columns[column] * 1.04)
			# plt.plot(range(13096), values.loc[:(13096 - 1), column], 'b', range(13096, 41496), values.loc[13096:(41496-1), column], 'r', range(41496, len(values)), values.loc[41496:, column], 'b')
			plt.plot(range(failure_interval[0]), values.loc[:(failure_interval[0] - 1), column].rolling(roll_window).mean(), 'b', range(failure_interval[0], failure_interval[1]), values.loc[failure_interval[0]:(failure_interval[1]-1), column].rolling(roll_window).mean(), 'r', range(failure_interval[1], len(values)), values.loc[failure_interval[1]:, column].rolling(roll_window).mean(), 'b')
			plt.axvline(x=28634)
			print('Saving')
			plt.gcf().set_size_inches(50, 10)
			plt.savefig('images/window_' + str(roll_window) + '/' + column + '.png', bbox_inches='tight')
			print('Saved')
	


# 2016-04-01 - 13096
# 2016-10-28 - 41469