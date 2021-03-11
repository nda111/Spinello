import pickle

from typing import Tuple

import app_config
import numpy as np
from sklearn.neural_network import MLPRegressor
from coil import Coil, derive_current


__second_order_coils__ = [
    Coil(np.float32(0.2), np.int32(100), up_vector=np.array((1, 0, 0))),
    Coil(np.float32(0.2), np.int32(100), up_vector=np.array((0, 1, 0))),
    Coil(np.float32(0.2), np.int32(100), up_vector=np.array((0, 0, 1))),
]


class Predictor:
    def __init__(self):
        with open(app_config.PATH_MODEL, 'rb') as model_file:
            self.model = pickle.load(model_file)

    def inference(self, position: np.ndarray, up_vector: np.ndarray) -> Tuple[np.ndarray]:
        pass


class SingleModelPredictor(Predictor):
    def inference(self, position: np.ndarray, up_vector: np.ndarray) -> Tuple[np.ndarray, np.ndarray]:
        model: MLPRegressor = self.model

        first_order_coil = Coil(np.float32(0.5), np.int32(50), position=position, up_vector=up_vector)
        currents = np.array([
            -derive_current(
                first_order_coil, second_order_coil,
                delta_voltage=np.float32(100), delta_time=np.int32(1))
            for second_order_coil
            in __second_order_coils__]).reshape(1, -1)

        pred = model.predict(currents)[0]
        return pred[:3], pred[3:]


class DoubleModelPredictor(Predictor):
    def inference(self, position: np.ndarray, up_vector: np.ndarray) -> Tuple[np.ndarray, np.ndarray]:
        pos_model: MLPRegressor = self.model[0]
        up_model: MLPRegressor = self.model[1]

        first_order_coil = Coil(np.float32(0.5), np.int32(50), position=position, up_vector=up_vector)
        currents = np.array([
            -derive_current(
                first_order_coil, second_order_coil,
                delta_voltage=np.float32(100), delta_time=np.int32(1))
            for second_order_coil
            in __second_order_coils__]).reshape(1, -1)

        return pos_model.predict(currents)[0], up_model.predict(currents)[0]
