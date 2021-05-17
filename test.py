import torch
from torch.utils.data import Dataset, DataLoader

t = torch.FloatTensor([1, 2, 3])
norm = torch.sqrt(torch.dot(t, t))

normalized = t / norm
norm_norm = torch.sqrt(torch.dot(normalized, normalized))

print(t, norm, normalized, norm_norm)
