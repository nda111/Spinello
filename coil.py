import torch
import math
from typing import List


class Coil:
    """
    A coil represented by its position, up vector, the cross-sectional area and the number of winding.
    """

    @staticmethod
    def calc_up_vector(latitude: float, longitude: float) -> torch.FloatTensor:
        """
        Calculate the up-vector from latitude and longitude.

        :param latitude: The horizontal angle on a sphere.
        :param longitude: The vertical angle on a sphere.

        :return: The up-vector.
        """
        vertical_cos = math.cos(longitude)
        return torch.FloatTensor((
            vertical_cos * math.cos(latitude),
            vertical_cos * math.sin(latitude),
            math.sin(longitude)
        ))


    @staticmethod
    def calc_angles(up_vector: torch.FloatTensor) -> List[float]:
        """
        Calculate the longtitude and latitude from up-vector.

        :param up_vector: The up-vector.

        :return: A list: [longitude, latitude]
        """
        up = up_vector / torch.sqrt(torch.dot(up_vector, up_vector))
        longitude = math.acos(up[2])
        cos = math.sqrt(1 - up[2] * up[2])
        latitude = math.acos(up[0] / cos)
        return [latitude, longitude]

    def __init__(self,
                 area: float,
                 num_wind: int,
                 position: torch.FloatTensor = torch.zeros(3),
                 up_vector: torch.FloatTensor = torch.FloatTensor([0, 0, 1])):
        """
        Create a coil.

        :param area: The cross-sectional area of the coil in meter squared in meters squared.
        :param num_wind: The number the coil wound up.
        :param position: The location of the center of the coil.
        :param up_vector: The direction of the derived flow.
        """
        self.area = area
        self.num_wind = num_wind
        self.position = position
        self.up_vector = up_vector

    def __copy__(self):
        """
        Deep copy this object and return the clone.

        :return: The clone object.
        """
        return Coil(self.area, self.num_wind, self.position, self.up_vector)


def derive_current(first: Coil, second: Coil, delta_voltage: float, delta_time: float) -> float:
    """
    Drive electric field from the first-order coil to the second-order scoil.

    :param first: The first-order coil.
    :param second: The second-order coil.
    :param delta_voltage: The voltage progress by delta_time in volts.
    :param delta_time: The interval between two chronological points in seconds.

    :returns The voltage of the derived current in volts.
    """
    # Calculate the norm of each vectors and dot product between them to get the angle.
    first_norm_up = first.up_vector.dot(first.up_vector)
    second_norm_up = second.up_vector.dot(second.up_vector)
    dot_between = first.up_vector.dot(second.up_vector)

    # Calculate the sin value to reflect the angle.
    cos = dot_between / torch.sqrt(first_norm_up * second_norm_up)
    sin = torch.sqrt(1 - cos * cos)

    # Calculate the distance between two coils.
    dist_vector = first.position - second.position
    dist_squared = dist_vector.dot(dist_vector)
    if dist_squared == 0:
        return float('nan')

    # Calculate the current of the first-order coil.
    flux = first.num_wind * first.area * delta_voltage

    # Calculate the simply derived current without angle and distance.
    derived_flux = -second.num_wind * second.area * flux / delta_time

    # Apply the angle and the distance and return.
    return derived_flux * sin / dist_squared
