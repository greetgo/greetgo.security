package kz.greetgo.security.util;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class ITestListenerAbstract implements ITestListener {

  protected void printNote(String note) {
    try {
      Files.write(
        Paths.get(System.getProperty("BUILD_DIR") + "/show_note.txt"),
        note.getBytes(StandardCharsets.UTF_8)
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onTestStart(ITestResult result) {}

  @Override
  public void onTestSuccess(ITestResult result) {}

  @Override
  public void onTestFailure(ITestResult result) {}

  @Override
  public void onTestSkipped(ITestResult result) {}

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

  @Override
  public void onStart(ITestContext context) {}

  @Override
  public void onFinish(ITestContext context) {}
}
