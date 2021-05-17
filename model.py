import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
from typing import List


class TrainDataset(Dataset):
    def __init__(self, X, y):
        self.x_data = X
        self.y_data = y

    def __len__(self):
        return len(self.x_data)

    def __getitem__(self, idx):
        x = torch.FloatTensor(self.x_data[idx])
        y = torch.FloatTensor(self.y_data[idx])
        return x, y


class PositionRegression:
    def __init__(self):
        self.model = nn.Sequential(
            nn.Linear(3, 32),
            # nn.BatchNorm1d(32),
            nn.ReLU(),

            nn.Linear(32, 32),
            # nn.BatchNorm1d(32),
            nn.ReLU(),

            nn.Linear(32, 32),
            # nn.BatchNorm1d(32),
            nn.ReLU(),

            nn.Linear(32, 32),
            # nn.BatchNorm1d(32),
            nn.ReLU(),

            nn.Linear(32, 32),
            # nn.BatchNorm1d(32),
            nn.ReLU(),

            nn.Linear(32, 6),
        )

    def init_model(self):
        self.__init__()

    def train(self, X: torch.FloatTensor, y: torch.FloatTensor, learning_rate: float, epochs: int, batch_size: int,
              verbose=False):
        if epochs < 1:
            raise ValueError('Must train at least once.')

        optimizer = optim.Adam(self.model.parameters(), lr=learning_rate, betas=(0.5, 0.999), eps=1.0E-7)
        criteria = nn.MSELoss()
        for epoch in range(1, epochs + 1):
            if batch_size <= 0:
                hypothesis = self.model(X)
                cost = criteria(hypothesis, y)

                optimizer.zero_grad()
                cost.backward()
                optimizer.step()

                if verbose:
                    print(f'Epoch {epoch}/{epochs}, Cost={cost.tolist()}')
            else:
                dataset = TrainDataset(X, y)
                data_loader = DataLoader(dataset, shuffle=True, batch_size=batch_size, drop_last=True)
                num_batches = len(data_loader)

                for batch_idx, samples in enumerate(data_loader):
                    x_train, y_train = samples

                    hypothesis = self.model(x_train)
                    cost = criteria(hypothesis, y_train)

                    optimizer.zero_grad()
                    cost.backward()
                    optimizer.step()

                    if verbose:
                        if batch_idx % 100 == 0:
                            print(f'Epoch {epoch}/{epochs} Batch {batch_idx}/{num_batches}, Cost={cost.tolist()}')

        hypothesis = self.model(X)
        loss = criteria(hypothesis, y)
        print(f'Training done. Loss={loss}')

    def predict(self, X: torch.FloatTensor):
        return self.model(X)
