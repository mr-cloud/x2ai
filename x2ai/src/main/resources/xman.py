import os

import cPickle
import random

import pandas as pd
import numpy as np
import time

import sys
from sklearn.cross_validation import train_test_split
from sklearn.ensemble import AdaBoostClassifier
from sklearn.ensemble import ExtraTreesClassifier
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.ensemble import RandomForestClassifier

import XConstant
import ntpath

# from sklearn.externals import joblib

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
        if algoName == self.algos[0]:
            clf = RandomForestClassifier(n_estimators=50)
        elif algoName == self.algos[1]:
            clf = AdaBoostClassifier(n_estimators=50)
        elif algoName == self.algos[2]:
            clf = ExtraTreesClassifier(n_estimators=50)
        elif algoName == self.algos[3]:
            clf = GradientBoostingClassifier(n_estimators=50, learning_rate=1.0, max_depth=1, random_state=0)
        else:
            print(algoName + ' not supported yet.')
            return
        self.scoreFileHandler.write('<' + str(time.time()) + '>'  + algoName + ':\n')
        print('classifier initialized.')
        return self.xFlow(clf, data, target)

    def xFlow(self, clf, features, target):
        # 80% training, 20% testing
        feature_train, feature_test, target_train, target_test = train_test_split(features, target, test_size=0.2)
        print('start training...')
        try:
            clf = clf.fit(feature_train, target_train)
        except ValueError:
            print('Oops! Cannot train this model with the input data.')
            self.writeScoreTofile('null')
            return None
        print('finished traning!')

        print('start testing on cv...')
        accuracyCv = clf.score(feature_test, target_test)
        print('finished testing on cv!')
        print('the average accuracy on cv examples is ' + str(accuracyCv))

        print('start testing on all examples...')
        accuracyAll = clf.score(features, target)
        print('finished testing on all examples!')
        print('the average accuracy on all examples is ' + str(accuracyAll))

        self.writeScoreTofile("cv:" + str(accuracyCv) + ",all:" + str(accuracyAll) + '\n')
        return clf

    def writeScoreTofile(self, score):
        self.scoreFileHandler.write(str(score))
        self.scoreFileHandler.write('\n')

    def predict(self, algoName=XConstant.randomized_algo, inputData=XConstant.test_data_predict, delims='\s+', dataType=np.str, outputData=XConstant.recommendation_dir, model_memento_dir=XConstant.model_memento_dir):
        print('prediction model: ' + algoName)
        # load data
        print('start loading data...')
        rawData = pd.read_csv(inputData, sep=delims, dtype=dataType)
        dim = rawData.shape
        print('size of data: (%d, %d)' % (dim[0], dim[1]))
        data = rawData.ix[:, 0:dim[1]].astype('float')
        # check model in memento
        mementoPath = model_memento_dir + algoName + XConstant.model_memento_postfix
        if os.path.isfile(mementoPath):
            print('restoring model from memento...')
            with open(mementoPath, 'rb') as fid:
                clf = cPickle.load(fid)
            # clf = joblib.load(mementoPath)
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

    def trainAll(self, inputData=XConstant.test_data):
            for algo in self.algos:
                self.train(algoName=algo, inputData=inputData)

# test
# xman = XMan()
# xman.trainAll()
# xman.predict(algoName=XConstant.classifier_RandomForest)
# xman.predict(algoName=XConstant.classifier_AdaBoost)
# xman.predict(algoName=XConstant.classifier_ExtraTrees)
# xman.predict(algoName=XConstant.classifier_GradientBoosting)
#
# xman.scoreFileHandler.close()

if __name__ == '__main__':
    print('%s was activated.' % __file__)
    xman = XMan()
    argsN = sys.argv.__len__()
    print('system args\' length: ' + str(argsN))
    actionArg = sys.argv[1]
    print('action type: ' + str(actionArg))
    if actionArg == XConstant.action_training:
        # TODO...
        if argsN != 4:
            print('input args error!')
        else:
            algoNameArg = sys.argv[2]
            inputDataArg = sys.argv[3]
            # TODO
            # print('done.')
            xman.train(algoName=algoNameArg, inputData=inputDataArg)
    elif actionArg == XConstant.action_precdition:
        if argsN != 6:
            print('input args error!')
        else:
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
    elif actionArg == XConstant.action_train_all:
        # TODO...
        if argsN != 3:
            print('input args error!')
        else:
            inputDataArg = sys.argv[2]
            xman.trainAll(inputData=inputDataArg)
    else:
        print('Sorry, cannot deal with the action you named! We will catch up later!')