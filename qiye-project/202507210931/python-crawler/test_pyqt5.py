from PyQt5.QtWidgets import QApplication, QLabel, QWidget

app = QApplication([])
window = QWidget()
label = QLabel("Hello PyQt5!", window)
window.setWindowTitle("Test PyQt5")
window.show()
app.exec_()