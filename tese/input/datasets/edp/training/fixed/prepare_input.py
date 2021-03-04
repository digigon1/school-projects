import itertools
from datetime import datetime

import matplotlib.pyplot as plt

from scipy.stats import pearsonr, spearmanr

import pandas

correlate = False

failures = pandas.read_csv('failures.csv', delimiter=';')
metmast = pandas.read_csv('metmast.csv', delimiter=';')
signals = pandas.read_csv('signals.csv', delimiter=';')


signals = signals.set_index(['Timestamp'])
metmast = metmast.set_index(['Timestamp'])
sig_met = signals.join(metmast, how='outer')
sig_met = sig_met.reset_index()
sig_met = sig_met.set_index(['Timestamp', 'Turbine_T1', 'Turbine_T6', 'Turbine_T7', 'Turbine_T9', 'Turbine_T11'])
failures = failures.set_index(['Timestamp', 'Turbine_T1', 'Turbine_T6', 'Turbine_T7', 'Turbine_T9', 'Turbine_T11'])

# Drop useless data from metmast
sig_met = sig_met.drop(columns=['Min_Raindetection', 'Max_Raindetection', 'Avg_Raindetection', 'Anemometer1_Freq', 'Anemometer1_Offset', 'Anemometer1_CorrGain', 'Anemometer1_CorrOffset', 'Anemometer2_Freq', 'Anemometer2_Offset', 'Anemometer2_CorrGain', 'Anemometer2_CorrOffset', 'DistanceAirPress', 'AirRessureSensorZeroOffset'])

# Drop useless data from signals
sig_met = sig_met.drop(columns=['Prod_LatestAvg_ActPwrGen2', 'Prod_LatestAvg_ReactPwrGen2'])

# Drop incomplete data
sig_met = sig_met.drop(columns=['Anemometer1_Avg_Freq', 'Anemometer2_Avg_Freq', 'Avg_AmbientTemp', 'Avg_Humidity', 'Avg_Precipitation', 'Avg_Pressure', 'Avg_Winddirection2', 'Avg_Windspeed1', 'Avg_Windspeed2', 'Max_AmbientTemp', 'Max_Humidity', 'Max_Precipitation', 'Max_Pressure', 'Max_Winddirection2', 'Max_Windspeed1', 'Max_Windspeed2', 'Min_AmbientTemp', 'Min_Humidity', 'Min_Precipitation', 'Min_Pressure', 'Min_Winddirection2', 'Min_Windspeed1', 'Min_Windspeed2', 'Pressure_Avg_Freq', 'Var_Winddirection2', 'Var_Windspeed1', 'Var_Windspeed2'])

plt.matshow(sig_met.corr())
plt.savefig('test.png', bbox_inches='tight')

if correlate:
	# try to find correlations
	correlated = []
	total = len(sig_met.columns.values)
	total *= (total - 1)
	total /= 2
	i = 1
	for pair in itertools.combinations(sig_met.columns.values, 2):
		print(str(i) + '/' + str(total))
		series1 = sig_met[pair[0]].tolist()
		series2 = sig_met[pair[1]].tolist()
		p_corr, p_corr_p = pearsonr(series1, series2)
		# s_corr, s_corr_p = spearmanr(series1, series2)
		# if abs(p_corr) > 0.7 or abs(s_corr) > 0.7:
		if abs(p_corr) > 0.7:
		# if abs(s_corr) > 0.7:
			print(pair)
			print('Pearson: ' + str(p_corr) + ', ' + str(p_corr_p))
			correlated.append((p_corr, pair))
			# print('Spearman: ' + str(s_corr) + ', ' + str(s_corr_p))
			# correlated.append((s_corr, pair))
		i += 1

	correlated.sort()

	with open('corr.txt', 'w') as f:
		f.write(str(correlated))

sig_met_fail = sig_met.join(failures, how='outer')
sig_met_fail = sig_met_fail.fillna({'Component_GENERATOR':0, 'Component_HYDRAULIC_GROUP': 0, 'Component_GENERATOR_BEARING': 0, 'Component_TRANSFORMER': 0, 'Component_GEARBOX': 0})


sig_met_fail = sig_met_fail.reset_index()


# sig_met_fail['Component_NONE'] = 0
# sig_met_fail.loc[(sig_met_fail['Component_GENERATOR'] == 1) | (sig_met_fail['Component_HYDRAULIC_GROUP'] == 1) | (sig_met_fail['Component_GENERATOR_BEARING'] == 1) | (sig_met_fail['Component_TRANSFORMER'] == 1) | (sig_met_fail['Component_GEARBOX'] == 1), 'Component_NONE'] = 0

replacement_costs = {'Component_GENERATOR': 60000, 'Component_HYDRAULIC_GROUP': 20000, 'Component_GENERATOR_BEARING': 30000, 'Component_TRANSFORMER': 50000, 'Component_GEARBOX': 100000}
repair_costs = {'Component_GENERATOR': 15000, 'Component_HYDRAULIC_GROUP': 3000, 'Component_GENERATOR_BEARING': 12500, 'Component_TRANSFORMER': 3500, 'Component_GEARBOX': 20000}
inspection_costs = {'Component_GENERATOR': 5000, 'Component_HYDRAULIC_GROUP': 2000, 'Component_GENERATOR_BEARING': 4500, 'Component_TRANSFORMER': 1500, 'Component_GEARBOX': 5000}

# replacement_costs = {'Component_GENERATOR': 1, 'Component_HYDRAULIC_GROUP': 1, 'Component_GENERATOR_BEARING': 1, 'Component_TRANSFORMER': 1, 'Component_GEARBOX': 1}
# repair_costs = {'Component_GENERATOR': 0.25, 'Component_HYDRAULIC_GROUP': 0.15, 'Component_GENERATOR_BEARING': 5/12, 'Component_TRANSFORMER': 0.07, 'Component_GEARBOX': 0.2}
# inspection_costs = {'Component_GENERATOR': 1/12, 'Component_HYDRAULIC_GROUP': 0.1, 'Component_GENERATOR_BEARING': 0.15, 'Component_TRANSFORMER': 0.03, 'Component_GEARBOX': 0.02}


last_day = {}
turbines = ['Turbine_T1', 'Turbine_T6', 'Turbine_T7', 'Turbine_T9', 'Turbine_T11']
components = ['Component_GENERATOR', 'Component_HYDRAULIC_GROUP', 'Component_GENERATOR_BEARING', 'Component_TRANSFORMER', 'Component_GEARBOX']
for t in turbines:
	last_day[t] = {}
	for c in components:
		last_day[t][c] = datetime.now()
	
for index, row in reversed(list(sig_met_fail.iterrows())):
	print(index)
	for t in turbines:
		if row[t] > 0:
			date = datetime.strptime(row['Timestamp'], "%Y-%m-%dT%H:%M:%S")
			for c in components:
				if row[c] > 0:
					last_day[t][c] = date
					break
			
			for c in components:
				delta_fail_time = (abs(date - last_day[t][c]).total_seconds()) / (60 * 60 * 24)
				if delta_fail_time >= 2 and delta_fail_time <= 60:
					sig_met_fail.loc[index, c] = delta_fail_time / 60
					# sig_met_fail.loc[index, c] = replacement_costs[c] - (repair_costs[c] + ((replacement_costs[c] - repair_costs[c]) * (1 - (delta_fail_time / 60))))
					# sig_met_fail.loc[index, c] = 1
				else:
					# sig_met_fail.loc[index, c] = -1
					# sig_met_fail.loc[index, c] = -inspection_costs[c]
					sig_met_fail.loc[index, c] = 0
			break
	


with open('final_complete_input.csv', 'w') as f:
	f.write(sig_met_fail.to_csv(index=False))
	
with open('final_input_T1.csv', 'w') as f:
	f.write(sig_met_fail[sig_met_fail['Turbine_T1'] > 0].drop(columns=['Timestamp', 'Turbine_T1', 'Turbine_T6', 'Turbine_T7', 'Turbine_T9', 'Turbine_T11']).to_csv(index=False))
	
with open('final_input_T6.csv', 'w') as f:
	f.write(sig_met_fail[sig_met_fail['Turbine_T6'] > 0].drop(columns=['Timestamp', 'Turbine_T1', 'Turbine_T6', 'Turbine_T7', 'Turbine_T9', 'Turbine_T11']).to_csv(index=False))

with open('final_input_T7.csv', 'w') as f:
	f.write(sig_met_fail[sig_met_fail['Turbine_T7'] > 0].drop(columns=['Timestamp', 'Turbine_T1', 'Turbine_T6', 'Turbine_T7', 'Turbine_T9', 'Turbine_T11']).to_csv(index=False))
	
with open('final_input_T9.csv', 'w') as f:
	f.write(sig_met_fail[sig_met_fail['Turbine_T9'] > 0].drop(columns=['Timestamp', 'Turbine_T1', 'Turbine_T6', 'Turbine_T7', 'Turbine_T9', 'Turbine_T11']).to_csv(index=False))
	
with open('final_input_T11.csv', 'w') as f:
	f.write(sig_met_fail[sig_met_fail['Turbine_T11'] > 0].drop(columns=['Timestamp', 'Turbine_T1', 'Turbine_T6', 'Turbine_T7', 'Turbine_T9', 'Turbine_T11']).to_csv(index=False))
	