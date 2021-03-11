import pickle
import app_config
import numpy as np
from coil import Coil
import preprocess as pp
from sklearn.neural_network import MLPRegressor
import os


def new_sample() -> list:
    """
    Generate a sample data set.

    :return: The generated data set.
    """
    return pp.generate_train_set(
        n=app_config.PREF_NUM_DATA,
        second_order_coil=Coil(np.float32(0.2), np.int32(100)),
        first_order_coil=Coil(np.float32(0.5), np.int32(50)),
        delta_volt_per_seconds=100)


def create_mlp() -> MLPRegressor:
    """
    Create a multi-perceptron layers model.

    :return: The created model.
    """
    return MLPRegressor(hidden_layer_sizes=(100, 100, 100, 100, 100), max_iter=1000, verbose=1)


def optimize_model(n: int, single_model: bool):
    """
    Find the most accurate model by trying many training with same data set.
    And store the best model to a file.

    :param single_model: To make model for single predictor.
    :param n: The number of trial.
    """
    if os.path.exists(app_config.PATH_DATA):
        with open(app_config.PATH_DATA, 'rb') as data_file:
            train_set = test_set = pickle.load(data_file)
    else:
        train_set = test_set = new_sample()
        with open(app_config.PATH_DATA, 'wb') as data_file:
            pickle.dump(train_set, data_file)

    min_idx = -1
    min_error = 10.0E+100
    for i in range(n):
        print(f'Training #{i + 1}')
        if single_model:
            print('Fitting model.')
            model = create_mlp()
            model.fit(train_set[0], train_set[1])

            print('Evaluating model.')
            true_y = test_set[1]
            pred_y = np.array(model.predict(test_set[0]))

            error = np.zeros(6)
            for true, pred in zip(true_y, pred_y):
                error += true - pred
            error /= len(train_set)

            pos_error = np.max(np.abs(error[:3]))
            up_error = np.max(np.abs(error[3:]))
        else:
            pos_model = create_mlp()
            up_model = create_mlp()
            model = [pos_model, up_model]

            print('Processing dataset.')
            out_pos, out_up = [], []
            for datum in train_set[1]:
                out_pos.append(datum[:3])
                out_up.append(datum[3:])
            out_pos, out_up = np.array(out_pos), np.array(out_up)

            print('Fitting position model.')
            pos_model.fit(train_set[0], out_pos)
            print('Fitting up-vector model.')
            up_model.fit(train_set[0], out_up)

            pred_pos = pos_model.predict(out_pos)
            pred_up = up_model.predict(out_up)

            print('Evaluating model.')
            pos_error, up_error = np.zeros(3, dtype=np.float32), np.zeros(3, dtype=np.float32)
            for true, pred in zip(out_pos, pred_pos):
                pos_error += true - pred
            for true, pred in zip(out_up, pred_up):
                up_error += true - pred
            pos_error = np.max(pos_error / len(train_set))
            up_error = np.max(up_error / len(train_set))

        error = max(pos_error, up_error)

        print(f'#{i}\npos_err = {pos_error} %\nup_err = {up_error} %\nerror = {error} %\n', end='')

        if error < min_error:
            min_idx = i
            min_error = error
            with open(app_config.PATH_MODEL, 'wb') as file:
                pickle.dump(model, file)

    print('Result:', min_idx, min_error, '%')