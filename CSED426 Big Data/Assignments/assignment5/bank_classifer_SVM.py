import pandas as pd;
import numpy as np;
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import f1_score;
import time

TRAINING_FILE = "bank_train.csv";
TEST_FILE = "bank_test.csv";

df_train = pd.read_csv(TRAINING_FILE, header=0);
df_test = pd.read_csv(TEST_FILE, header=0);

#Remove rows from dataframe where job column has values unknown, unknown values are less in number 191 and 
#corresponding columns also contain -1 or unknown values for pdays and poutcome
#df_train = df_train[df_train.job != 'unknown']
#df_train = df_train[df_train.education != 'unknown']

job_enc = LabelEncoder()
df_train['job'] = job_enc.fit_transform(df_train['job'])

marital_enc = LabelEncoder()
df_train['marital'] = marital_enc.fit_transform(df_train['marital'])

edu_enc = LabelEncoder()
df_train['education'] = edu_enc.fit_transform(df_train['education'])

contact_enc = LabelEncoder()
df_train['contact'] = contact_enc.fit_transform(df_train['contact'])

month_enc = LabelEncoder()
df_train['month'] = month_enc.fit_transform(df_train['month'])

poutcome_enc = LabelEncoder()
df_train['poutcome'] = poutcome_enc.fit_transform(df_train['poutcome'])

df_train['defaulttransformed'] = pd.np.where(df_train['default'].astype(str)=="yes",1,0)
df_train['housingtransformed'] = pd.np.where(df_train['housing'].astype(str)=="yes",1,0)
df_train['loantransformed'] = pd.np.where(df_train['loan'].astype(str)=="yes",1,0)


job_enc2 = LabelEncoder()
df_test['job'] = job_enc2.fit_transform(df_test['job'])

marital_enc2 = LabelEncoder()
df_test['marital'] = marital_enc2.fit_transform(df_test['marital'])

edu_enc2 = LabelEncoder()
df_test['education'] = edu_enc2.fit_transform(df_test['education'])

contact_enc2 = LabelEncoder()
df_test['contact'] = contact_enc2.fit_transform(df_test['contact'])

month_enc2 = LabelEncoder()
df_test['month'] = month_enc2.fit_transform(df_test['month'])

poutcome_enc2 = LabelEncoder()
df_test['poutcome'] = poutcome_enc2.fit_transform(df_test['poutcome'])

df_test['defaulttransformed'] = pd.np.where(df_test['default'].astype(str)=="yes",1,0)
df_test['housingtransformed'] = pd.np.where(df_test['housing'].astype(str)=="yes",1,0)
df_test['loantransformed'] = pd.np.where(df_test['loan'].astype(str)=="yes",1,0)

df_train = df_train.drop(['default','housing','loan','pdays','poutcome'],axis=1)
df_test = df_test.drop(['default','housing','loan','pdays','poutcome'],axis=1)

attr_cols = df_train.columns
attr_cols = attr_cols.drop(['y'])

df_test_copy = df_test.copy()
df_test_copy = df_test_copy.drop(attr_cols, axis=1)

svclassifier = SVC(kernel="rbf", class_weight="balanced", C=1.0, random_state=0) 
svc = svclassifier.fit(df_train[attr_cols], df_train['y'])
df_test_copy['y'] = svc.predict(df_test[attr_cols])

df_test_copy.to_csv('sample_bank_submission_SVM_20182095.csv', index=False)