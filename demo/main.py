import app_config
import pickle
import delta_time as tm
import predictor
from OpenGL.GL import *
from OpenGL.GLU import *
from OpenGL.GLUT import *
import torch
from coil import Coil
from sklearn.neural_network import MLPRegressor

vertices = ((0, 0, 0), (0, 0, 1), (1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0))
edges = ((0, 1), (2, 3), (4, 5))

colors_rgb = (app_config.COLOR_FORE_X, app_config.COLOR_FORE_Y, app_config.COLOR_FORE_Z)
colors_white = (app_config.COLOR_WHITE, app_config.COLOR_WHITE, app_config.COLOR_WHITE)
colors_light_gray = (app_config.COLOR_LIGHT_GRAY, app_config.COLOR_LIGHT_GRAY, app_config.COLOR_LIGHT_GRAY)

position = torch.FloatTensor((-2.5, 0, 0))
angle = torch.zeros(2, dtype=torch.float)

used_keys = (
    b'w', b'a', b's', b'd', b'e', b'c',
    int(GLUT_KEY_LEFT), int(GLUT_KEY_RIGHT), int(GLUT_KEY_UP), int(GLUT_KEY_DOWN))
key_flag = {k: False for k in used_keys}


def lookAt():
    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()
    gluLookAt(0, 30, 10, 0, 0, 0, 0, 0, 1)


def drawAnchor(colors):
    for edge, color in zip(edges, colors):
        glColor3fv(color)
        glBegin(GL_LINES)
        for vertex in edge:
            glVertex3fv(vertices[vertex])
        glEnd()


def onKeyDown(key, _, __):
    global key_flag
    if key in key_flag:
        key_flag[key] = True


def onKeyUp(key, _, __):
    global key_flag
    if key in key_flag:
        key_flag[key] = False


def update(delta_time: float):
    rotation = torch.zeros(2, dtype=torch.float)
    translation = torch.zeros(3, dtype=torch.float)

    rotation_delta = app_config.PREF_ROTATION_STEP * delta_time
    translation_delta = app_config.PREF_TRANSLATION_STEP * delta_time

    if key_flag[int(GLUT_KEY_LEFT)]:  # Rot Y
        rotation[0] += rotation_delta
        if rotation[0] > 360:
            rotation[0] -= 360
    if key_flag[int(GLUT_KEY_RIGHT)]:  # Rot Y
        rotation[0] -= rotation_delta
        if rotation[0] < 0:
            rotation[0] += 360
    if key_flag[int(GLUT_KEY_UP)]:  # Rot X
        rotation[1] += rotation_delta
        if rotation[1] > 360:
            rotation[1] -= 360
    if key_flag[int(GLUT_KEY_DOWN)]:  # Rot X
        rotation[1] -= rotation_delta
        if rotation[1] < 0:
            rotation[1] += 360

    if key_flag[b'd']:  # Left
        translation[0] -= translation_delta
    if key_flag[b'a']:  # Right
        translation[0] += translation_delta
    if key_flag[b'w']:  # Fore
        translation[1] -= translation_delta
    if key_flag[b's']:  # Back
        translation[1] += translation_delta
    if key_flag[b'c']:  # Down
        translation[2] -= translation_delta
    if key_flag[b'e']:  # Up
        translation[2] += translation_delta

    global angle, position
    angle += rotation
    position += translation


def render():
    tm.update()
    update(tm.get_delta())
    lookAt()

    # Predict position, angle
    angle_r = torch.deg2rad(angle)
    up = Coil.calc_up_vector(angle_r[0], angle_r[1])

    # pred = predictor.DoubleModelPredictor()
    pred = predictor.TorchPredictor()
    pred_pos, pred_up = pred.inference(position, up)
    pred_angle = Coil.calc_angles(pred_up)

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    # Origin anchor
    drawAnchor(colors_rgb)

    # Real position
    glPushMatrix()
    glTranslatef(position[0], position[1], position[2])
    glRotatef(angle[0], 0, 1, 0)
    glRotatef(angle[1], 1, 0, 0)
    drawAnchor(colors_white)
    glPopMatrix()

    # Predicted position
    glPushMatrix()
    glTranslatef(pred_pos[0], pred_pos[1], pred_pos[2])
    glRotatef(pred_angle[0], 0, 1, 0)
    glRotatef(pred_angle[1], 1, 0, 0)
    drawAnchor(colors_light_gray)
    glPopMatrix()

    print(position, pred_pos)

    glutSwapBuffers()


def onSizeChanged(width: int, height: int):
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glViewport(0, 0, width, height)
    gluPerspective(45.0, width / height, 0.1, 1000.0)

    lookAt()


def main():
    glutInit()
    glutInitDisplayMode(GLUT_DEPTH | GLUT_DOUBLE | GLUT_RGBA)
    glutInitWindowSize(app_config.WINDOW_WIDTH, app_config.WINDOW_HEIGHT)
    glutCreateWindow(app_config.WINDOW_TITLE)

    glutReshapeFunc(onSizeChanged)
    glutKeyboardFunc(onKeyDown)
    glutSpecialFunc(onKeyDown)
    glutKeyboardUpFunc(onKeyUp)
    glutSpecialUpFunc(onKeyUp)

    glEnable(GL_DEPTH_TEST)

    lookAt()

    glutDisplayFunc(render)
    glutIdleFunc(render)

    tm.init()
    glutMainLoop()


if __name__ == "__main__":
    if os.path.exists(app_config.PATH_MODEL):
        # Load the model from a file.
        with open(app_config.PATH_MODEL, 'rb') as model_file:
            model: MLPRegressor = pickle.load(model_file)
        main()
    else:
        print('Please train a model before run this demo.')
