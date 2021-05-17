import pickle
import app_config
import torch
from coil import Coil
import preprocess as pp
from model import PositionRegression
import os


def new_sample() -> list:
    """
    Generate a sample data set.

    :return: The generated data set.
    """
    return pp.generate_train_set(
        n=app_config.PREF_NUM_DATA,
        second_order_coil=Coil(0.2, 100),
        first_order_coil=Coil(0.5, 50),
        delta_volt_per_seconds=100)


def optimize_model(epochs: int, learning_rate: float, batch_size: int):
    """
    Find the most accurate model by trying many training with same data set.
    And store the best model to a file.

    :param batch_size: The size of each bathces. 0 if won't use stochastic gradient descent.
    :param epochs: The number of trial.
    :param learning_rate: Learning rate for training the model.
    """
    if os.path.exists(app_config.PATH_DATA):
        with open(app_config.PATH_DATA, 'rb') as data_file:
            train_set = test_set = pickle.load(data_file)
    else:
        train_set = test_set = new_sample()
        with open(app_config.PATH_DATA, 'wb') as data_file:
            pickle.dump(train_set, data_file)

    model = PositionRegression()
    model.train(train_set[0], train_set[1], learning_rate=learning_rate, epochs=epochs, batch_size=batch_size, verbose=True)

    print('Evaluating model.')
    true_y = test_set[1]
    pred_y = model.predict(test_set[0])

    error = (true_y - pred_y).mean(dim=0)
    pos_error = (error[:3] / app_config.PREF_SPACE_SIZE * 100.0).tolist()
    up_error = (error[3:] * 100.0).tolist()
    error = error.tolist()

    print(f'Result: pos_err = {pos_error} %\nup_err = {up_error} %\nerror = {error} %\n', end='')
    with open(app_config.PATH_MODEL, 'wb') as model_file:
        pickle.dump(model, model_file)
