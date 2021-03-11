import pickle
import app_config
import numpy as np
import preprocess as pp
import train
import os
from coil import Coil, derive_current


train.optimize_model(1, False)
