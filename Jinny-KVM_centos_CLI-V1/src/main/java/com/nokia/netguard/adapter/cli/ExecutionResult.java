package com.nokia.netguard.adapter.cli;

import com.nakina.adapter.base.agent.api.base.RequestFailure;

public class ExecutionResult {

    private static final Integer SUCCESSFUL_EXECUTION_EXIT_CODE = 0;

    private static final Integer EXIT_CODE_NOT_CHECKED = -1;

    private String output;

    private String cleanOutput;

    private Integer exitCode = EXIT_CODE_NOT_CHECKED;

    public ExecutionResult(String output, String cleanOutput) {
        this.output = output;
        this.cleanOutput = cleanOutput;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getCleanOutput() {
        return cleanOutput;
    }

    public void setCleanOutput(String cleanOutput) {
        this.cleanOutput = cleanOutput;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public boolean isSuccessful() throws RequestFailure {
        if (exitCode == EXIT_CODE_NOT_CHECKED) {
            throw new RequestFailure("Cannot determine whether execution was successful or not, because exit code" +
                    " retrieval was not requested for a command.");
        }
        return exitCode == SUCCESSFUL_EXECUTION_EXIT_CODE;
    }
}
