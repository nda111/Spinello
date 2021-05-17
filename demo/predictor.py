import pickle

from typing import Tuple

import app_config
import torch
from sklearn.neural_network import MLPRegressor
from coil import Coil, derive_current
from model import PositionRegression


__second_order_coils__ = [
    Coil(0.2, 100, up_vector=torch.FloatTensor((1, 0, 0))),
    Coil(0.2, 100, up_vector=torch.FloatTensor((0, 1, 0))),
    Coil(0.2, 100, up_vector=torch.FloatTensor((0, 0, 1))),
]


class Predictor:
    def __init__(self):
        with open(app_config.PATH_MODEL, 'rb') as model_file:
            self.model = pickle.load(model_file)

    def inference(self, position: torch.FloatTensor, up_vector: torch.FloatTensor) -> Tuple[torch.FloatTensor]:
        pass


class SingleModelPredictor(Predictor):
    def inference(self, position: torch.FloatTensor, up_vector: torch.FloatTensor) -> Tuple[torch.FloatTensor, torch.FloatTensor]:
        model: MLPRegressor = self.model

        first_order_coil = Coil(0.5, 50, position=position, up_vector=up_vector)
        currents = torch.FloatTensor([
            -derive_current(
                first_order_coil, second_order_coil,
                delta_voltage=100, delta_time=1)
            for second_order_coil
            in __second_order_coils__]).reshape(1, -1)

        pred = model.predict(currents)[0]
        return pred[:3], pred[3:]


class DoubleModelPredictor(Predictor):
    def inference(self, position: torch.FloatTensor, up_vector: torch.FloatTensor) -> Tuple[torch.FloatTensor, torch.FloatTensor]:
        pos_model: MLPRegressor = self.model[0]
        up_model: MLPRegressor = self.model[1]

        first_order_coil = Coil(0.5, 50, position=position, up_vector=up_vector)
        currents = torch.FloatTensor([
            -derive_current(
                first_order_coil, second_order_coil,
                delta_voltage=100, delta_time=1)
            for second_order_coil
            in __second_order_coils__]).reshape(1, -1)

        return pos_model.predict(currents)[0], up_model.predict(currents)[0]


class TorchPredictor(Predictor):
    def inference(self, position: torch.FloatTensor, up_vector: torch.FloatTensor) -> Tuple[torch.FloatTensor, torch.FloatTensor]:
        model: PositionRegression = self.model

        first_order_coil = Coil(0.5, 50, position=position, up_vector=up_vector)
        currents = torch.FloatTensor([
            -derive_current(
                first_order_coil, second_order_coil,
                delta_voltage=100, delta_time=1)
            for second_order_coil
            in __second_order_coils__]).view(-1, 3)

        pred = model.predict(currents)[0].tolist()
        return torch.FloatTensor(pred[:3]) * app_config.PREF_SPACE_SIZE, torch.FloatTensor(pred[3:])
