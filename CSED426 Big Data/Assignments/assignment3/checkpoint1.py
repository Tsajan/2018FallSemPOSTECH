#import necessary libraries
import pandas as pd;
import numpy as np;
from sklearn.preprocessing import LabelEncoder;
from sklearn.tree import DecisionTreeClassifier;
from sklearn import tree;
import time

#training and test file constant definitions
TRAINING_FILE = "crime_train.csv";
TEST_FILE = "crime_test.csv";

#load into dataframe
df_train = pd.DataFrame.from_csv(TRAINING_FILE, parse_dates=True, index_col=None);
df_test = pd.DataFrame.from_csv(TEST_FILE, parse_dates=True, index_col=None);

#drop unnecessary columns
df_train = df_train.drop(['Descript', 'Resolution'], axis=1)

#define a custom date column, in order to perform manipulation
df_train['customdate'] = pd.to_datetime(df_train['Dates'], format = "%Y-%m-%d %H:%M:%S")

#extract time related information on training dataset
df_train['Day'] = df_train['customdate'].dt.day
df_train['Month'] = df_train['customdate'].dt.month
df_train['Year'] = df_train['customdate'].dt.year
df_train['Hour'] = df_train['customdate'].dt.hour
df_train['DayOfWeek'] = df_train['customdate'].dt.dayofweek
df_train['WeekOfYear'] = df_train['customdate'].dt.weekofyear
df_train['isIntersection'] = pd.np.where(df_train.Address.str.contains("/"),1,0)
df_train['isBlock'] = pd.np.where(df_train.Address.str.contains("Block"),1,0)

#define a custom date column, in order to perform manipulation
df_test['customdate'] = pd.to_datetime(df_test['Dates'], format = "%Y-%m-%d %H:%M:%S")

#extract time related information on training dataset
df_test['Day'] = df_test['customdate'].dt.day
df_test['Month'] = df_test['customdate'].dt.month
df_test['Year'] = df_test['customdate'].dt.year
df_test['Hour'] = df_test['customdate'].dt.hour
df_test['DayOfWeek'] = df_test['customdate'].dt.dayofweek
df_test['WeekOfYear'] = df_test['customdate'].dt.weekofyear
df_test['isIntersection'] = pd.np.where(df_test.Address.str.contains("/"),1,0)
df_test['isBlock'] = pd.np.where(df_test.Address.str.contains("Block"),1,0)

#encoder to convert PdDistrict categorical value to a numerical value
train_encoder = LabelEncoder()
df_train['PdDistrict'] = train_encoder.fit_transform(df_train['PdDistrict'])

#encoder to convert PdDistrict categorical value to a numerical value
test_encoder = LabelEncoder()
df_test['PdDistrict'] = test_encoder.fit_transform(df_test['PdDistrict'])

#encoder to convert Category into a numerical value
category_encoder = LabelEncoder() 
category_encoder.fit(df_train['Category']) 
df_train['CategoryEncoded'] = category_encoder.transform(df_train['Category']) 
print(category_encoder.classes_)

#remove customdate and address columns
df_train = df_train.drop(['customdate','Address'],axis=1)
df_test = df_test.drop(['customdate','Address'], axis=1)

df_train['rot45X'] = 0.707 * df_train['X'] + 0.707 * df_train['Y']
df_train['rot45Y'] = 0.707 * df_train['Y'] - 0.707 * df_train['X']
df_train['rot30X'] = (1.732/2) * df_train['X'] + (1.0/2) * df_train['Y']
df_train['rot30Y'] = (1.732/2) * df_train['Y'] - (1.0/2) * df_train['X']
df_train['rot60X'] = (1.0/2) * df_train['X'] + (1.732/2) * df_train['Y']
df_train['rot60Y'] = (1.0/2) * df_train['Y'] - (1.732/2) * df_train['X']
df_train['radial_r'] = np.sqrt(np.power(df_train['Y'],2) + np.power(df_train['X'],2))

#list of attribute columns, we are going to use
attr_cols = list(df_train.columns[2:-1])
print(attr_cols)

#initialize decision tree and predict probabilities
clf = tree.DecisionTreeClassifier()
clf = clf.fit(df_train[attr_cols], df_train['CategoryEncoded'])
predictions = np.array(clf.predict_proba(df_test[attr_cols]))

#checking output 
print(predictions)
print(predictions.shape)

#writing to file
result = pd.DataFrame(predictions, columns=category_encoder.classes_)
result.to_csv('test_sample_submission_' + time.strftime('%Y%m%d-%H%M%S') + '.csv', index = True, index_label = 'id' )