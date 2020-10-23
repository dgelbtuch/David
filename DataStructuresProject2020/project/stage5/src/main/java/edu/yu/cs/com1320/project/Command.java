package edu.yu.cs.com1320.project;

import java.net.URI;
import java.util.function.Function;

public final class Command
{
    /**the URI of the document this command was executed on*/
    private URI uri;
    private Function<URI,Boolean> undo;
    private Function<URI,Boolean> redo;
    public Command(URI uri, Function<URI,Boolean> undo, Function<URI,Boolean> redo)
    {
        this.uri = uri;
        this.undo = undo;
        this.redo = redo;
    }

    /**@return the URI of the document this command was executed on*/
    public URI getUri()
    {
        return this.uri;
    }

    public boolean undo()
    {
        return undo.apply(this.uri);
    }

    public boolean redo()
    {
        return redo.apply(this.uri);
    }
}
