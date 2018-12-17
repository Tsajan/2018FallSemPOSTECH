import numpy as np
import pandas as pd

from sklearn import preprocessing as pp
from sklearn.svm import SVC
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn import metrics

import read_csv

TRAINING_FILE = "crime_train.csv";
TEST_FILE = "crime_test.csv";

df_train = pd.read_csv(TRAINING_FILE, header=0, parse_dates=['Dates'])
df_test = pd.read_csv(TEST_FILE, header=0, parse_dates=['Dates'])

id_col = df_test[['id']].copy()
id_col = id_col.astype(float)

#feature_engineering for training dataset

df_train['Year'] = df_train['Dates'].map(lambda x: x.year)
df_train['Month'] = df_train['Dates'].map(lambda x: x.month)
df_train['Week'] = df_train['Dates'].map(lambda x: x.week)
df_train['DayOfWeek'] = df_train['Dates'].map(lambda x: x.dayofweek)
df_train['Hour'] = df_train['Dates'].map(lambda x: x.hour)
df_train['isDark'] = df_train['Hour'].apply(lambda x: 1 if (x >= 18 or x < 6) else 0)
df_train['StreetNo'] = df_train['Address'].apply(lambda x: x.split(' ', 1)[0] if x.split(' ', 1)[0].isdigit() else 0)
df_train['isIntersection'] = pd.np.where(df_train.Address.str.contains("/"),1,0)
df_train['isBlock'] = pd.np.where(df_train.Address.str.contains("Block"),1,0)

#defining polar co-ordinates for better classification- polar co-ordinates at 30, 45 and 60 degrees
xy_scaler = pp.StandardScaler()
xy_scaler.fit(df_train[["X", "Y"]])
df_train[["X", "Y"]] = xy_scaler.transform(df_train[["X", "Y"]])
df_train["rot45_X"] = .707 * df_train["Y"] + .707 * df_train["X"]
df_train["rot45_Y"] = .707 * df_train["Y"] - .707 * df_train["X"]
df_train["rot30_X"] = (1.732 / 2) * df_train["X"] + (1. / 2) * df_train["Y"]
df_train["rot30_Y"] = (1.732 / 2) * df_train["Y"] - (1. / 2) * df_train["X"]
df_train["rot60_X"] = (1. / 2) * df_train["X"] + (1.732 / 2) * df_train["Y"]
df_train["rot60_Y"] = (1. / 2) * df_train["Y"] - (1.732 / 2) * df_train["X"]
df_train["radial_r"] = np.sqrt(np.power(df_train["Y"], 2) + np.power(df_train["X"], 2))

#converting the values to 2 significant decimal places
df_train.X = df_train.X.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_train.Y = df_train.Y.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_train.rot45_X = df_train.rot45_X.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_train.rot45_Y = df_train.rot45_Y.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_train.rot30_X = df_train.rot30_X.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_train.rot30_Y = df_train.rot30_Y.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_train.rot60_X = df_train.rot60_X.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_train.rot60_Y = df_train.rot60_Y.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_train.radial_r = df_train.radial_r.map(lambda x: "%.2f" % round(x, 2)).astype(float)

#encoder to convert PdDistrict categorical value to a numerical value
train_pd_encoder = LabelEncoder()
df_train['PdDistrict'] = train_pd_encoder.fit_transform(df_train['PdDistrict'])

#encode nominal data in category column of training dataset to represent as different number for different class
category_encoder = LabelEncoder() 
category_encoder.fit(df_train['Category']) 
df_train['CategoryEncoded'] = category_encoder.transform(df_train['Category']) 

#feature_engineering for test dataset
df_test['Year'] = df_test['Dates'].map(lambda x: x.year)
df_test['Month'] = df_test['Dates'].map(lambda x: x.month)
df_test['Week'] = df_test['Dates'].map(lambda x: x.week)
df_test['DayOfWeek'] = df_test['Dates'].map(lambda x: x.dayofweek)
df_test['Hour'] = df_test['Dates'].map(lambda x: x.hour)
df_test['isDark'] = df_test['Hour'].apply(lambda x: 1 if (x >= 18 or x < 6) else 0)
df_test['StreetNo'] = df_test['Address'].apply(lambda x: x.split(' ', 1)[0] if x.split(' ', 1)[0].isdigit() else 0)
df_test['isIntersection'] = pd.np.where(df_test.Address.str.contains("/"),1,0)
df_test['isBlock'] = pd.np.where(df_test.Address.str.contains("Block"),1,0)

#defining polar co-ordinates for better classification- polar co-ordinates at 30, 45 and 60 degrees
xy_scaler = pp.StandardScaler()
xy_scaler.fit(df_test[["X", "Y"]])
df_test[["X", "Y"]] = xy_scaler.transform(df_test[["X", "Y"]])
df_test["rot45_X"] = .707 * df_test["Y"] + .707 * df_test["X"]
df_test["rot45_Y"] = .707 * df_test["Y"] - .707 * df_test["X"]
df_test["rot30_X"] = (1.732 / 2) * df_test["X"] + (1. / 2) * df_test["Y"]
df_test["rot30_Y"] = (1.732 / 2) * df_test["Y"] - (1. / 2) * df_test["X"]
df_test["rot60_X"] = (1. / 2) * df_test["X"] + (1.732 / 2) * df_test["Y"]
df_test["rot60_Y"] = (1. / 2) * df_test["Y"] - (1.732 / 2) * df_test["X"]
df_test["radial_r"] = np.sqrt(np.power(df_test["Y"], 2) + np.power(df_test["X"], 2))

#converting the values to 2 significant decimal places
df_test.X = df_test.X.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_test.Y = df_test.Y.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_test.rot45_X = df_test.rot45_X.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_test.rot45_Y = df_test.rot45_Y.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_test.rot30_X = df_test.rot30_X.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_test.rot30_Y = df_test.rot30_Y.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_test.rot60_X = df_test.rot60_X.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_test.rot60_Y = df_test.rot60_Y.map(lambda x: "%.2f" % round(x, 2)).astype(float)
df_test.radial_r = df_test.radial_r.map(lambda x: "%.2f" % round(x, 2)).astype(float)

#encoder to convert PdDistrict categorical value to a numerical value
test_pd_encoder = LabelEncoder()
df_test['PdDistrict'] = test_pd_encoder.fit_transform(df_test['PdDistrict'])

df_train = df_train.drop(['Resolution','Address'],axis=1)
df_test = df_test.drop(['Address'],axis=1)

df_test = df_test.drop(['Dates'], axis=1)

attr_cols = df_train.columns[3:-1]
print(attr_cols)

df_test[attr_cols]

svmObj = SVC(kernel="rbf", class_weight="balanced", C=1.0, random_state=0)
svm = svmObj.fit(df_train[attr_cols], df_train['CategoryEncoded'])
predictions = svm.predict_proba(df_test[attr_cols]).astype(float)

predictions
predictions.shape

cats = pd.DataFrame(predictions, columns=category_encoder.classes_)
result = pd.concat([id_col, cats], axis=1)
result
result.to_csv('sample_crime_submission_SVM_20182095.csv', index = False)