import os

import cPickle
import random

import pandas as pd
import numpy as np
import time

import sys

from sklearn import cross_validation
from sklearn.cross_validation import train_test_split
from sklearn.ensemble import AdaBoostClassifier
from sklearn.ensemble import ExtraTreesClassifier
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report
from sklearn.preprocessing import OneHotEncoder

import XConstant
import ntpath

# from sklearn.externals import joblib
from plot_learning_curve import plot_learning_curve
from grid_search import  grid_search

if __name__ == '__main__':
    print('%s was activated.' % __file__)
    class XMan:

        def __init__(self, inputFileName=XConstant.test_data):
            self.inputFileName = inputFileName
            self.scoreFileHandler = open(XConstant.score_file_name, mode='a')
            self.algos = [
                XConstant.classifier_RandomForest,
                XConstant.classifier_AdaBoost,
                XConstant.classifier_ExtraTrees,
                XConstant.classifier_GradientBoosting
            ]

        def train(self, algoName=XConstant.randomized_algo, inputData=XConstant.test_data, delims='\s+', dataType=np.str, model_memento_dir=XConstant.model_memento_dir):
            print('training model: ' + algoName)
            # load data
            print('start loading data...')
            rawData = pd.read_csv(inputData, dtype=dataType, sep=delims)
            dim = rawData.shape
            print('size of data: (%d, %d)' % (dim[0], dim[1]))
            target = rawData.ix[:, 0].astype('float')
            data = rawData.ix[:, 1:dim[1]].astype('float')
            # one-hot-encoding
            data = self.oneHotEncoder(data)
            print('data loaded.')
            # check model in memento
            mementoPath = model_memento_dir + algoName + XConstant.model_memento_postfix
            # train data
            print('start training model for ' + algoName + '...')
            clf = self.algo(algoName, data, target)
            # save new model to memento
            if clf is not None:
                # check number of classes
                if clf.n_classes_ != 2:
                    print('the number of classes trained just now is ' + str(clf.n_classes_) + "\nmodel was abandoned.")
                    return False

                if not os.path.isdir(model_memento_dir):
                    os.mkdir(model_memento_dir)
                if os.path.isfile(mementoPath):
                    print('clean old model from memento...')
                    os.remove(mementoPath)
                print('store model to memento.')
                with open(mementoPath, 'wb') as fid:
                    cPickle.dump(clf, fid)
                # joblib.dump(clf, mementoPath)
                return True
            else:
                print('model training failed.')
                return False

        def algo(self, algoName, data, target):
            print('initializing classifier...')
            tuned_parameters = None
            if algoName == self.algos[0]:
                clf = RandomForestClassifier(n_estimators=100, min_samples_leaf=100, min_samples_split=100)
                # tuning hyper-parameters
                # tuned_parameters = [{
                #    'n_estimators': [10, 20, 50, 100, 200],
                #    'min_samples_split': [2, 10, 30, 100],
                #    'min_samples_leaf':[1, 3, 10, 30, 100, 300]
                # }]
            elif algoName == self.algos[1]:
                clf = AdaBoostClassifier(n_estimators=50)
            elif algoName == self.algos[2]:
                clf = ExtraTreesClassifier(n_estimators=50)
            elif algoName == self.algos[3]:
                clf = GradientBoostingClassifier(n_estimators=50)
            else:
                print(algoName + ' not supported yet.')
                return
            self.scoreFileHandler.write('<' + str(time.time()) + '>'  + algoName + ':\n')
            print('classifier initialized.')
            return self.xFlow(clf, data, target, tuned_parameters)

        def xFlow(self, clf, features, target, tuned_parameters=None):
            # 80% training, 20% testing
            feature_train, feature_test, target_train, target_test = train_test_split(features, target, test_size=0.2)
            print('start training...')
            try:
                if tuned_parameters is not None:
                    clf = grid_search(clf=clf, X=feature_train, y=target_train, tuned_parameters=tuned_parameters)
                else:
                    clf = clf.fit(feature_train, target_train)
            except ValueError:
                print('Oops! Cannot train this model with the input data.')
                self.writeScoreTofile('null')
                return None
            print('finished traning!')

            print('start testing on cv...')
            accuracyCv = clf.score(feature_test, target_test)
            y_true, y_pred = target_test, clf.predict(feature_test)
            test_report = classification_report(y_true, y_pred)
            print('finished testing on cv!')
            print('the average accuracy on cv examples is ' + str(accuracyCv))
            print('test report:\n' + test_report)
            print('start testing on all examples...')
            accuracyAll = clf.score(features, target)
            print('finished testing on all examples!')
            print('the average accuracy on all examples is ' + str(accuracyAll))
            # training results are stored to file.
            self.writeScoreTofile("cv:" + str(accuracyCv) + ",all:" + str(accuracyAll) + '\n')
            self.writeScoreTofile("test report:\n" + test_report)
            return clf

        def writeScoreTofile(self, score):
            self.scoreFileHandler.write(str(score))
            self.scoreFileHandler.write('\n')

        def oneHotEncoder(self, features):
            # change float to int for some columns, e.g. 'city'.
            intColsInd = [5]
            intCols = features.ix[:, intColsInd].astype('float').astype('int')
            for i, col in enumerate(intColsInd):
                features.ix[:, col] = intCols.ix[:, i]
            # one-hot-encoder for categorical features.
            ohe = OneHotEncoder(categorical_features=[5], sparse=False)
            columns = features.columns.values.tolist()
            for popper in intColsInd:
                columns.pop(popper)
            ohe.fit(features)
            new_columns = []
            # mamxium number(count from 1) of values left semi branket [1,  n_values_)
            for i in range(ohe.n_values_ - 1):
                new_columns.append('city' + str(i + 1))
            new_columns.extend(columns)
            transformed_input = ohe.transform(features)
            features = pd.DataFrame(transformed_input, columns=new_columns)
            return features

        def predict(self, algoName=XConstant.randomized_algo, inputData=XConstant.test_data_predict, delims='\s+', dataType=np.str, outputData=XConstant.recommendation_dir, model_memento_dir=XConstant.model_memento_dir):
            print('prediction model: ' + algoName)
            # load data
            print('start loading data...')
            rawData = pd.read_csv(inputData, sep=delims, dtype=dataType)
            dim = rawData.shape
            print('size of data: (%d, %d)' % (dim[0], dim[1]))
            data = rawData.ix[:, 0:dim[1]].astype('float')
            # one-hot-encoding
            data = self.oneHotEncoder(data)
            # check model in memento
            mementoPath = model_memento_dir + algoName + XConstant.model_memento_postfix
            if os.path.isfile(mementoPath):
                print('restoring model from memento...')
                with open(mementoPath, 'rb') as fid:
                    clf = cPickle.load(fid)
                # clf = joblib.load(mementoPath)
                # number of estimators
                print('number of estimators: ' + str(clf.estimators_.__len__()))
                # number of classes
                print('number of classes: ' + str(clf.n_classes_))
                if clf.n_classes_ == 2:
                    label1_ind = 1
                else:
                    label1_ind = 0
                # predict classes
                print('labels prediction:\n')
                predict_classes = clf.predict(data)
                for lab in predict_classes:
                    print(str(lab) + ", ")
                # predict probabilities
                print('probabilities prediction:\n')
                proba_mutil_labels = clf.predict_proba(data)
                for probas in proba_mutil_labels:
                    print(str(probas[label1_ind]) + ',')
                proba_binary_label = []
                for arr in proba_mutil_labels:
                    proba_binary_label.append(float("{0:.2f}".format(arr[label1_ind])))
                indexSortedProba = [(ent[0], ent[1]) for ent in sorted(enumerate(proba_binary_label), key=lambda x: x[1], reverse=True)]
            else:
                # recommended by random algorithm.
                print(algoName + ' not supported yet.\nrecommended by random.')
                proba = range(0, len(data.index))
                random.shuffle(proba)
                indexSortedProba = [(ele, 1) for ele in proba]
            if not os.path.isdir(outputData):
                os.mkdir(outputData)
            resPath = outputData  + ntpath.basename(inputData)
            if os.path.isfile(resPath):
                os.remove(resPath)
            print('store recommendation result:')
            with open(resPath, mode='w') as fid:
                for ent in indexSortedProba:
                    print(str(ent[0]) + '\t' + str(ent[1]))
                    fid.write(str(ent[0]) + '\t' + str(ent[1]) + '\n')
            print('prediction finished.')

        def trainAll(self, inputData=XConstant.test_data, model_memento_dir=XConstant.model_memento_dir):
                for algo in self.algos:
                    self.train(algoName=algo, inputData=inputData, model_memento_dir=model_memento_dir)

        def findEstimator(self, algoName):
            clf = None
            if algoName == self.algos[0]:
                clf = RandomForestClassifier(n_estimators=100, min_samples_leaf=100, min_samples_split=100)
            elif algoName == self.algos[1]:
                clf = AdaBoostClassifier(n_estimators=50)
            elif algoName == self.algos[2]:
                clf = ExtraTreesClassifier(n_estimators=50)
            elif algoName == self.algos[3]:
                clf = GradientBoostingClassifier(n_estimators=50, learning_rate=1.0, max_depth=1, random_state=0)
            else:
                print(algoName + ' not supported yet.')
            return clf

        def plot_lc(self, algoName, inputData=XConstant.test_data, title='learning curve'):
            clf = self.findEstimator(algoName)
            if clf is not None:
                # load data
                print('start loading data...')
                delims = '\s+'
                dataType = np.str
                rawData = pd.read_csv(inputData, dtype=dataType, sep=delims)
                dim = rawData.shape
                print('size of data: (%d, %d)' % (dim[0], dim[1]))
                target = rawData.ix[:, 0].astype('float')
                data = rawData.ix[:, 1:dim[1]].astype('float')
                data = xman.oneHotEncoder(data)
                print('data loaded.')
                cv = cross_validation.ShuffleSplit(dim[0], n_iter=10,
                                                   test_size=0.2, random_state=0)
                plt = plot_learning_curve(clf, title, data, target, ylim=(0.0, 1.01), cv=cv, n_jobs=4)
                #plt.show()
                if not os.path.exists(XConstant.lc_dir):
                    os.mkdir(XConstant.lc_dir)
                plt.savefig(XConstant.lc_dir + algoName + '_' + str(time.time()) + '.png')
                print('learning curve drawing finished.')
            else:
                print('learning curve drawing failed.')




    # test
    # print('create a x-man!')
    # xman = XMan()
    # xman.trainAll()
    # xman.train(algoName=XConstant.classifier_RandomForest, inputData='lease_data.txt')
    # xman.predict(algoName=XConstant.classifier_RandomForest)
    # xman.predict(algoName=XConstant.classifier_AdaBoost)
    # xman.predict(algoName=XConstant.classifier_ExtraTrees)
    # xman.predict(algoName=XConstant.classifier_GradientBoosting)

    # test learning curve
    # xman = XMan()
    # xman.plot_lc(XConstant.classifier_RandomForest)
    # print('test finished.')

    # xman.scoreFileHandler.close()

    xman = XMan()
    argsN = sys.argv.__len__()
    print('system args\' length: ' + str(argsN))
    actionArg = sys.argv[1]
    print('action type: ' + str(actionArg))

    if actionArg == XConstant.action_training:
        # TODO...
        if argsN == 5:
            algoNameArg = sys.argv[2]
            inputDataArg = sys.argv[3]
            mementoDirArg = sys.argv[4]
            xman.train(algoName=algoNameArg, inputData=inputDataArg, model_memento_dir=mementoDirArg)
        elif argsN == 4:
            algoNameArg = sys.argv[2]
            inputDataArg = sys.argv[3]
            xman.train(algoName=algoNameArg, inputData=inputDataArg)
        elif argsN == 3:
            algoNameArg = sys.argv[2]
            xman.train(algoName=algoNameArg)
        else:
            print('input args error!')

    elif actionArg == XConstant.action_precdition:
        if argsN == 6:
            algoNameArg = sys.argv[2]
            inputDataArg = sys.argv[3]
            outputDataArg = sys.argv[4]
            modelMementoDirArg = sys.argv[5]
            print("input args:")
            for arg in sys.argv:
                print(arg + "\n")
            # TODO
            # print('done')
            xman.predict(algoName=algoNameArg, inputData=inputDataArg, outputData=outputDataArg, model_memento_dir=modelMementoDirArg)
        elif argsN == 3:
            algoNameArg = sys.argv[2]
            xman.predict(algoName=algoNameArg)
        else:
            print('input args error!')

    elif actionArg == XConstant.action_train_all:
        if argsN == 4:
            inputDataArg = sys.argv[2]
            mementoDirArg = sys.argv[3]
            xman.trainAll(inputData=inputDataArg, model_memento_dir=mementoDirArg)
        elif argsN == 3:
            inputDataArg = sys.argv[2]
            xman.trainAll(inputData=inputDataArg)
        elif argsN == 2:
            xman.trainAll()
        else:
            print('input args error!')

    elif actionArg == XConstant.action_plot_learning_curve:
        if argsN == 5:
            estimator = sys.argv[2]
            inputDataArg = sys.argv[3]
            title = sys.argv[4]
            xman.plot_lc(estimator, inputDataArg, title)
        elif argsN == 4:
            estimator = sys.argv[2]
            inputDataArg = sys.argv[3]
            xman.plot_lc(estimator, inputDataArg)
        elif argsN == 3:
            estimator = sys.argv[2]
            xman.plot_lc(estimator)
        else:
            print('input args error!')
    else:
        print('Sorry, cannot deal with the action you named! We will catch up later!')

    xman.scoreFileHandler.close()
