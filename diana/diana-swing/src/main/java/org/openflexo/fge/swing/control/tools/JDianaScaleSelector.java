package org.openflexo.fge.swing.control.tools;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openflexo.fge.control.AbstractDianaEditor;
import org.openflexo.fge.control.tools.DianaScaleSelector;
import org.openflexo.fge.swing.SwingViewFactory;

/**
 * SWING implementation of the {@link DianaScaleSelector}
 * 
 * @author sylvain
 * 
 */
public class JDianaScaleSelector extends DianaScaleSelector<JToolBar, SwingViewFactory> {

	private static final int MAX_ZOOM_VALUE = 300;
	protected JTextField scaleTF;
	protected JSlider slider;

	protected ChangeListener sliderChangeListener;
	protected ActionListener actionListener;

	private final JToolBar component;

	private boolean isInitialized = false;

	public JDianaScaleSelector(AbstractDianaEditor<?, SwingViewFactory, ?> editor) {
		super(editor);
		component = new JToolBar();
		component.setOpaque(false);
		scaleTF = new JTextField(5);
		scaleTF.setFont(new Font("SansSerif", Font.PLAIN, 9));
		int currentScale = (getEditor() != null ? (int) (getEditor().getScale() * 100) : 100);
		scaleTF.setText(currentScale + "%");
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, MAX_ZOOM_VALUE, currentScale);
		slider.setPreferredSize(new Dimension(200, 15));
		slider.setOpaque(false);
		slider.setMajorTickSpacing(100);
		slider.setMinorTickSpacing(20);
		slider.setPaintTicks(false/* true */);
		slider.setPaintLabels(false);
		slider.setBorder(BorderFactory.createEmptyBorder());
		sliderChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (slider.getValue() > 0) {
					getEditor().setScale((double) slider.getValue() / 100);
					scaleTF.removeActionListener(actionListener);
					scaleTF.setText("" + (int) (getEditor().getScale() * 100) + "%");
					scaleTF.addActionListener(actionListener);
				}
			}
		};
		actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// logger.info("On fait avec "+scaleTF.getText()+" ce qui donne: "+(((double)Integer.decode(scaleTF.getText()))/100));
					Integer newScale = null;
					if (scaleTF.getText().indexOf("%") > -1) {
						newScale = Integer.decode(scaleTF.getText().substring(0, scaleTF.getText().indexOf("%")));
					} else {
						newScale = Integer.decode(scaleTF.getText());
					}
					if (newScale > MAX_ZOOM_VALUE) {
						newScale = MAX_ZOOM_VALUE;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								scaleTF.setText(MAX_ZOOM_VALUE + "%");
							}
						});
					}
					getEditor().setScale((double) newScale / 100);
				} catch (NumberFormatException exception) {
					// Forget
				}
				scaleTF.removeActionListener(actionListener);
				slider.removeChangeListener(sliderChangeListener);
				scaleTF.setText("" + (int) (getEditor().getScale() * 100) + "%");
				slider.setValue((int) (getEditor().getScale() * 100));
				slider.addChangeListener(sliderChangeListener);
				scaleTF.addActionListener(actionListener);
			}
		};
		scaleTF.addActionListener(actionListener);
		slider.addChangeListener(sliderChangeListener);
		component.add(slider);
		component.add(scaleTF);
		isInitialized = true;
		// setBorder(BorderFactory.createEmptyBorder());
	}

	@Override
	public JToolBar getComponent() {
		return component;
	}

	@Override
	public void handleScaleChanged() {
		if (isInitialized) {
			slider.setValue((int) (getEditor().getScale() * 100));
		}
	}

	@Override
	public SwingViewFactory getDianaFactory() {
		return SwingViewFactory.INSTANCE;
	}

}